package com.zzh.yummy.config;

import com.android.build.api.dsl.BuildType;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.SoftwareComponent;

import java.util.ArrayList;
import java.util.Collection;

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
        FatUtils.logAnytime("project=" + p);
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
        FatUtils.logAnytime("Creating configuration embed");
        // for (SoftwareComponent component : project.getComponents()) {
        //     FatUtils.logAnytime("component=" + component);
        // }

        LibraryExtension libraryExtension = project.getExtensions().findByType(LibraryExtension.class);
        FatUtils.logAnytime("libraryExtension=" + libraryExtension);

        // Object androidComponents2 = project.getExtensions().getByName("androidComponents");
        // FatUtils.logAnytime("androidComponents2=" + androidComponents2);

    }

    private void createConfiguration(Configuration embedConf) {
        embedConf.setVisible(false);
        embedConf.setTransitive(false);
        project.getGradle().addListener(new EmbedResolutionListener(project, embedConf));
        embedConfigurations.add(embedConf);
    }

    private void doBeforeEvaluate() {
        BaseExtension android = project.getExtensions().findByType(BaseExtension.class);
        FatUtils.logAnytime("do before evaluate android=" + android);
    }

    private void doAfterEvaluate() {
        AndroidComponentsExtension androidComponentsExtension = project.getExtensions().findByType(AndroidComponentsExtension.class);
        FatUtils.logAnytime("androidComponentsExtension2=" + androidComponentsExtension);


        try {
            BaseExtension android = android();
            // android.registerTransform(transform);
            for (BuildType buildType : android.getBuildTypes()) {
                String configName = buildType.getName() + CONFIG_SUFFIX;
                Configuration configuration = project.getConfigurations().create(configName);
                createConfiguration(configuration);
                FatUtils.logAnytime("Creating configuration " + configName);
            }
            //
            // project.android.productFlavors.all { flavor ->
            //         String configName = flavor.name + CONFIG_SUFFIX;
            //     Configuration configuration = project.configurations.create(configName);
            //     createConfiguration(configuration);
            //     FatUtils.logInfo("Creating configuration " + configName);
            //     project.android.buildTypes.all { buildType ->
            //             String variantName = flavor.name + buildType.name.capitalize();
            //         String variantConfigName = variantName + CONFIG_SUFFIX;
            //         Configuration variantConfiguration = project.configurations.create(variantConfigName);
            //         createConfiguration(variantConfiguration);
            //         FatUtils.logInfo("Creating configuration " + variantConfigName);
            //     }
            // }
        } catch (Exception e) {
            FatUtils.logAnytime("Project " + project.getName() + " get exception=" + e);
        }


    }

    private BaseExtension android() throws Exception {
        BaseExtension android = project.getExtensions().findByType(BaseExtension.class);
        if (android != null) {
            return android;
        } else {
            FatUtils.logAnytime("Project " + project.getName() + " is not an Android project");
            throw new Exception("Project " + project.getName() + " is not an Android project");
        }
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