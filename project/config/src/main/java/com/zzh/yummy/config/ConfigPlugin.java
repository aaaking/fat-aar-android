package com.zzh.yummy.config;

import com.android.build.api.dsl.BuildType;
import com.android.build.api.extension.impl.VariantSelectorImpl;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.api.variant.LibraryAndroidComponentsExtension;
import com.android.build.api.variant.LibraryVariant;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.internal.dsl.ProductFlavor;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Pair;

/**
 * author: zhouzhihui
 * created on: 2024/1/6 01:40
 * description:
 */
public class ConfigPlugin implements Plugin<Project> {
    public static final String ARTIFACT_TYPE_AAR = "aar";

    public static final String ARTIFACT_TYPE_JAR = "jar";

    private static final String CONFIG_NAME = "embed";

    public static final String CONFIG_SUFFIX = "Embed";

    private Project project;

    private RClassesTransform transform;

    private final Collection<Configuration> embedConfigurations = new ArrayList<>();

    @Override
    public void apply(Project p) {
        this.project = p;
        FatUtils.logAnytime("project=" + p + " gradle version=" + project.getGradle().getGradleVersion());
        FatUtils.attach(project);
        DirectoryManager.attach(project);
        project.getExtensions().create(FatAarExtension.NAME, FatAarExtension.class);
        createConfigurations();
        registerTransform();
        project.beforeEvaluate(project1 -> doBeforeEvaluate());
        project.afterEvaluate(project1 -> doAfterEvaluate());
    }

    private void registerTransform() {
        transform = new RClassesTransform(project);
        // register in project.afterEvaluate is invalid.
        // project.android.registerTransform(transform);
        // io.realm.transformer.RealmTransformer.CompanionObject.register(project);
    }

    private void createConfigurations() {
        Configuration embedConf = project.getConfigurations().create(CONFIG_NAME);
        createConfiguration(embedConf);
        // for (SoftwareComponent component : project.getComponents()) {
        //     FatUtils.logAnytime("component=" + component);
        // }

        // LibraryExtension libraryExtension = project.getExtensions().findByType(LibraryExtension.class);
        // FatUtils.logAnytime("libraryExtension=" + libraryExtension);

        // Object androidComponents2 = project.getExtensions().getByName("androidComponents");
        // FatUtils.logAnytime("androidComponents2=" + androidComponents2);
    }

    private void createConfiguration(Configuration embedConf) {
        embedConf.setVisible(false);
        embedConf.setTransitive(false);
        project.getGradle().addListener(new EmbedResolutionListener(project, embedConf));
        embedConfigurations.add(embedConf);
        FatUtils.logAnytime("Creating configuration " + embedConf.getName());
    }

    private void doBeforeEvaluate() {
        BaseExtension android = project.getExtensions().findByType(BaseExtension.class);
        FatUtils.logAnytime("do before evaluate android=" + android);
    }

    private void doAfterEvaluate() {
        AndroidComponentsExtension androidComponentsExtension = project.getExtensions().findByType(AndroidComponentsExtension.class);
        FatUtils.logAnytime("androidComponentsExtension2=" + androidComponentsExtension);
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        FatUtils.logAnytime("appExtension old=" + appExtension);
        LibraryExtension libraryExtension = project.getExtensions().findByType(LibraryExtension.class);
        FatUtils.logAnytime("libraryExtension old=" + libraryExtension + " " + (libraryExtension == null ? "" : libraryExtension.getLibraryVariants()));

        ApplicationAndroidComponentsExtension appExt2 = project.getExtensions().findByType(ApplicationAndroidComponentsExtension.class);
        FatUtils.logAnytime("appExt new=" + appExt2);
        LibraryAndroidComponentsExtension libExt = project.getExtensions().findByType(LibraryAndroidComponentsExtension.class);
        FatUtils.logAnytime("libExt new=" + libExt);

        try {
            BaseExtension android = FatUtils.android(project);

            // https://developer.android.com/build/releases/gradle-plugin-roadmap?hl=zh-cn
            // Transform API 没有单一的替代 API，每个用例都会有新的针对性 API。所有替代 API 都位于 androidComponents {} 代码块中，在 AGP 7.2 中均有提供。
            // android.registerTransform(transform);

            for (BuildType buildType : android.getBuildTypes()) {
                String configName = buildType.getName() + CONFIG_SUFFIX;
                Configuration configuration = project.getConfigurations().create(configName);
                createConfiguration(configuration);
            }
            for (ProductFlavor flavor : android.getProductFlavors()) {
                String configName = flavor.getName() + CONFIG_SUFFIX;
                Configuration configuration = project.getConfigurations().create(configName);
                createConfiguration(configuration);
                for (BuildType buildType : android.getBuildTypes()) {
                    String variantName = flavor.getName() + FatUtils.capitalize(buildType.getName());
                    String variantConfigName = variantName + CONFIG_SUFFIX;
                    Configuration variantConfiguration = project.getConfigurations().create(variantConfigName);
                    createConfiguration(variantConfiguration);
                }
            }

            // from fat aar do after evaluate
            FatAarExtension fatAarExtension = project.getExtensions().findByType(FatAarExtension.class);
            for (Configuration embedConfiguration : embedConfigurations) {
                if (fatAarExtension.transitive) {
                    embedConfiguration.setTransitive(true);
                }
            }
            if (libExt != null) {
                libExt.onVariants(new VariantSelectorImpl(), new Action<LibraryVariant>() {
                    @Override
                    public void execute(LibraryVariant newLibVar) {
                        // if ("release".equals(newLibVar.getName())) {
                        //     return;
                        // }
                        FatUtils.logAnytime("\n-------------");
                        // todo
                        // https://github.com/runningcode/fladle/issues/269
                        List<Pair<String, String>> list = newLibVar.getProductFlavors();
                        FatUtils.logAnytime("new lib variant product flavors=" + list + " name=" + newLibVar.getName() + " flavorname=" + newLibVar.getFlavorName() + " buildtype=" + newLibVar.getBuildType());
                        // if (list.isEmpty()) {
                        //     return;
                        // }
                        Collection<ResolvedArtifact> artifacts = new ArrayList();
                        Collection<ResolvedDependency> firstLevelDependencies = new ArrayList<>();
                        for (Configuration configuration : embedConfigurations) {
                            if (isMineConfiguration(configuration, newLibVar)) {
                                Collection<ResolvedArtifact> resolvedArtifacts = resolveArtifacts(configuration);
                                artifacts.addAll(resolvedArtifacts);
                                artifacts.addAll(dealUnResolveArtifacts(configuration, newLibVar, resolvedArtifacts));
                                firstLevelDependencies.addAll(configuration.getResolvedConfiguration().getFirstLevelModuleDependencies());
                            }
                        }

                        // if (!artifacts.isEmpty()) { // todo
                        //     VariantProcessor processor = new VariantProcessor(project, variant);
                        //     processor.processVariant(artifacts, firstLevelDependencies, transform);
                        // }
                    }
                });
            }
            if (libraryExtension != null) {
                for (com.android.build.gradle.api.LibraryVariant variant : libraryExtension.getLibraryVariants()) {
                    List<com.android.builder.model.ProductFlavor> list = variant.getProductFlavors();
                    com.android.builder.model.ProductFlavor mergedFlavor = variant.getMergedFlavor();
                    FatUtils.logAnytime("old lib variant product flavors=" + list + " name=" + variant.getName() + " flavorname=" + variant.getFlavorName() + " buildtype=" + variant.getBuildType().getName());
                    FatUtils.logAnytime("old lib variant merged flavor=" + mergedFlavor);
                }
            }
        } catch (Exception e) {
            FatUtils.logAnytime("Project " + project.getName() + " get exception=" + e);
        }


    }

    private Collection<ResolvedArtifact> resolveArtifacts(Configuration configuration) {
        ArrayList<ResolvedArtifact> list = new ArrayList();
        if (configuration != null) {
            Set<ResolvedArtifact> resolvedArtifactSet = configuration.getResolvedConfiguration().getResolvedArtifacts();
            FatUtils.logAnytime("\"" + configuration.getName() + "\"" + " resolve artifacts set=" + resolvedArtifactSet);
            for (ResolvedArtifact artifact : resolvedArtifactSet) {
                if (ARTIFACT_TYPE_AAR.equals(artifact.getType()) || ARTIFACT_TYPE_JAR.equals(artifact.getType())) {
                    //
                } else {
                    throw new ProjectConfigurationException("Only support embed aar and jar dependencies!", new Throwable());
                }
                list.add(artifact);
            }
        }
        FatUtils.logAnytime("resolve artifacts list=" + list);
        return list;
    }

    private Collection<ResolvedArtifact> dealUnResolveArtifacts(Configuration configuration, LibraryVariant variant, Collection<ResolvedArtifact> artifacts) {
        ArrayList<ResolvedArtifact> artifactList = new ArrayList();
        Set<ResolvedDependency> set = configuration.getResolvedConfiguration().getFirstLevelModuleDependencies();
        FatUtils.logAnytime("configuration=" + configuration.getName() + " get first level module dependencies=" + set);
        for (ResolvedDependency dependency : set) {
            boolean match = artifacts.stream().anyMatch(artifact ->
                    dependency.getModuleName().equals(artifact.getModuleVersion().getId().getName()));
            if (!match) {
                ResolvedArtifact flavorArtifact = FlavorArtifact.createFlavorArtifact(project, variant, dependency);
                if (flavorArtifact != null) {
                    artifactList.add(flavorArtifact);
                }
            }
        }
        FatUtils.logAnytime("deal unresolved artifacts list=" + artifactList);
        return artifactList;
    }

    private boolean isMineConfiguration(Configuration configuration, LibraryVariant variant) {
        return configuration.getName().equals(CONFIG_NAME)
                || configuration.getName().equals(variant.getBuildType() + CONFIG_SUFFIX)
                || configuration.getName().equals(variant.getFlavorName() + CONFIG_SUFFIX)
                || configuration.getName().equals(variant.getName() + CONFIG_SUFFIX);
    }

    private Collection<ResolvedArtifact> dealUnResolveArtifacts(Configuration configuration, com.android.build.gradle.api.LibraryVariant variant, Collection<ResolvedArtifact> artifacts) {
        ArrayList<ResolvedArtifact> artifactList = new ArrayList();
        for (ResolvedDependency dependency : configuration.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            boolean match = artifacts.stream().anyMatch(artifact ->
                    dependency.getModuleName().equals(artifact.getModuleVersion().getId().getName()));
            if (!match) {
                ResolvedArtifact flavorArtifact = null; //  FlavorArtifact.createFlavorArtifact(project, variant, dependency);
                if (flavorArtifact != null) {
                    artifactList.add(flavorArtifact);
                }
            }
        }
        return artifactList;
    }

    private boolean isMineConfiguration(Configuration configuration, com.android.build.gradle.api.LibraryVariant variant) {
        return configuration.getName().equals(CONFIG_NAME)
                || configuration.getName().equals(variant.getBuildType().getName() + CONFIG_SUFFIX)
                || configuration.getName().equals(variant.getFlavorName() + CONFIG_SUFFIX)
                || configuration.getName().equals(variant.getName() + CONFIG_SUFFIX);
    }

// fun BaseExtension.variants(): DomainObjectSet<out BaseVariant> {
//     return when (this) {
//         is AppExtension -> {
//             applicationVariants
//         }
//
//         is FeatureExtension ->{
//             featureVariants
//         }
//
//         is LibraryExtension -> {
//             libraryVariants
//         }
//
//    else -> throw GradleException("Unsupported BaseExtension type!")
//     }
// }
}