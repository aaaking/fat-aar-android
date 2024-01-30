package com.zzh.yummy.config;

import com.android.build.api.variant.LibraryAndroidComponentsExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.api.variant.LibraryVariant;
import com.android.builder.core.AbstractProductFlavor;
import com.android.builder.model.ProductFlavor;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.internal.tasks.TaskDependencyContainer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.DisplayName;
import org.gradle.internal.Factory;
import org.gradle.internal.component.model.DefaultIvyArtifactName;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import groovy.lang.Closure;
import kotlin.Pair;

/**
 * author: zhouzhihui
 * created on: 2024/1/18 11:32
 * description:
 */
public class FlavorArtifact {
    // since 6.8.0
    private static final String CLASS_PreResolvedResolvableArtifact = "org.gradle.api.internal.artifacts.PreResolvedResolvableArtifact";
    // since 6.8.0
    private static final String CLASS_CalculatedValueContainer = "org.gradle.internal.model.CalculatedValueContainer";

    private static final String CLASS_DefaultResolvedArtifact = "org.gradle.api.internal.artifacts.DefaultResolvedArtifact";

    public static ResolvedArtifact createFlavorArtifact(Project project, LibraryVariant variant, ResolvedDependency unResolvedArtifact) {
        Project artifactProject = getArtifactProject(project, unResolvedArtifact);
        TaskProvider bundleProvider = null;
        try {
            bundleProvider = getBundleTask(artifactProject, variant);
        } catch (Exception e) {
            FatUtils.logError(variant.getName() + " Can not resolve " + unResolvedArtifact.getModuleName() + " e=" + e);
            return null;
        }

        if (bundleProvider == null) {
            return null;
        }

        // ModuleVersionIdentifier identifier = createModuleVersionIdentifier(unResolvedArtifact)
        // File artifactFile = createArtifactFile(artifactProject, bundleProvider.get())
        // DefaultIvyArtifactName artifactName = createArtifactName(artifactFile)
        // Factory<File> fileFactory = new Factory<File>() {
        //     @Override
        //     File create() {
        //         return artifactFile
        //     }
        // }
        // ComponentArtifactIdentifier artifactIdentifier = createComponentIdentifier(artifactFile)
        // if (FatUtils.compareVersion(project.gradle.gradleVersion, "6.0.0") >= 0) {
        //     TaskDependencyContainer taskDependencyContainer = new TaskDependencyContainer() {
        //         @Override
        //         void visitDependencies(TaskDependencyResolveContext taskDependencyResolveContext) {
        //             taskDependencyResolveContext.add(createTaskDependency(bundleProvider.get()))
        //         }
        //     }
        //     if (FatUtils.compareVersion(project.gradle.gradleVersion, "6.8.0") >= 0) {
        //         Object fileCalculatedValue = Class.forName(CLASS_CalculatedValueContainer).newInstance(new DisplayName(){
        //             @Override
        //             String getCapitalizedDisplayName() {
        //                 return artifactFile.name
        //             }
        //
        //             @Override
        //             String getDisplayName() {
        //                 return artifactFile.name
        //             }
        //         }, artifactFile)
        //         return Class.forName(CLASS_PreResolvedResolvableArtifact).newInstance(
        //                 identifier,
        //                 artifactName,
        //                 artifactIdentifier,
        //                 fileCalculatedValue,
        //                 taskDependencyContainer,
        //                 null
        //         )
        //     } else {
        //         return Class.forName(CLASS_DefaultResolvedArtifact)
        //                 .newInstance(identifier, artifactName, artifactIdentifier, taskDependencyContainer, fileFactory)
        //     }
        // } else {
        //     TaskDependency taskDependency = createTaskDependency(bundleProvider.get())
        //     return Class.forName(CLASS_DefaultResolvedArtifact)
        //             .newInstance(identifier, artifactName, artifactIdentifier, taskDependency, fileFactory)
        // }

        return null; // todo
    }

    private static TaskProvider getBundleTask(Project project, LibraryVariant variant) {
        TaskProvider bundleTaskProvider = null;
        while (true) {
            try {
                BaseExtension android = FatUtils.android(project);
                LibraryAndroidComponentsExtension libExt = project.getExtensions().findByType(LibraryAndroidComponentsExtension.class);
                // 1. find same flavor
                try {
                    bundleTaskProvider = VersionAdapter.getBundleTaskProvider(project, variant.getName());
                    break;
                } catch (Exception e) {
                    FatUtils.logError("get bundle task provider step 1 error=" + e);
                }
                // 2. find buildType
                try {
                    bundleTaskProvider = VersionAdapter.getBundleTaskProvider(project, variant.getName());
                    break;
                } catch (Exception e) {
                    FatUtils.logError("get bundle task provider  step 2 error=" + e);
                }
                // 3. find missingStrategies
                List<Pair<String, String>> list = variant.getProductFlavors();
                for (Pair<String, String> pair : list) {
                    if (pair.getSecond().equals(variant.getName())) {
                        bundleTaskProvider = VersionAdapter.getBundleTaskProvider(project, variant.getName());
                    }
                }
            } catch (Exception e) {
                FatUtils.logError("get bundle task error=" + e);
            }
            break;
        }
        FatUtils.logAnytime("get bundle task result=" + bundleTaskProvider + " name=" + (bundleTaskProvider == null ? "null" : bundleTaskProvider.getName()));
        return bundleTaskProvider;
    }



    private static TaskProvider getBundleTask(Project project, com.android.build.gradle.api.LibraryVariant variant) {
        TaskProvider bundleTaskProvider = null;
        try {
            BaseExtension android = FatUtils.android(project);
            LibraryExtension libraryExtension = project.getExtensions().findByType(LibraryExtension.class);
            for (com.android.build.gradle.api.LibraryVariant subVariant : libraryExtension.getLibraryVariants()) {
                // 1. find same flavor
                if (Objects.equals(variant.getName(), subVariant.getName())) {
                    try {
                        bundleTaskProvider = VersionAdapter.getBundleTaskProvider(project, subVariant.getName());
                        break;
                    } catch (Exception e) {
                        FatUtils.logAnytime("get bundle task provider step 1 error=" + e);
                    }
                }
                // 2. find buildType
                ProductFlavor flavor = variant.getProductFlavors().isEmpty() ? variant.getMergedFlavor() : variant.getProductFlavors().stream().findFirst().get();
                if (Objects.equals(subVariant.getName(), variant.getBuildType().getName())) {
                    try {
                        bundleTaskProvider = VersionAdapter.getBundleTaskProvider(project, subVariant.getName());
                        break;
                    } catch (Exception e) {
                        FatUtils.logAnytime("get bundle task provider  step 2 error=" + e);
                    }
                }
                // 3. find missingStrategies
                try {
                    if (flavor instanceof AbstractProductFlavor) {
                        for (Map.Entry<String, AbstractProductFlavor.DimensionRequest> entry : ((AbstractProductFlavor) flavor).getMissingDimensionStrategies().entrySet()) {
                            String toDimension = entry.getKey();
                            List<String> list = new ArrayList<>(); // ((AbstractProductFlavor.DimensionRequest)entry.getValue()).getFallbacks();
                            String toFlavor = list.get(0);
                            ProductFlavor subFlavor = subVariant.getProductFlavors().isEmpty() ?
                                    subVariant.getMergedFlavor() : subVariant.getProductFlavors().get(0);
                            if (Objects.equals(toDimension, subFlavor.getDimension())
                                    && Objects.equals(toFlavor, subFlavor.getName())
                                    && variant.getBuildType().getName().equals(subVariant.getBuildType().getName())) {
                                try {
                                    bundleTaskProvider = VersionAdapter.getBundleTaskProvider(project, subVariant.getName());
                                    break;
                                } catch (Exception e) {
                                    FatUtils.logAnytime("get bundle task provider  step 3 error=" + e);
                                }
                            }
                        }
                    }
                } catch (Exception ignore) {

                }
            }
        } catch (Exception e) {
            FatUtils.logAnytime("get bundle task error=" + e);
        }
        return bundleTaskProvider;
    }

    private static TaskDependency createTaskDependency(Task bundleTask) {
        return new TaskDependency() {
            @Override
            public Set<? extends Task> getDependencies(@Nullable Task task) {
                Set set = new HashSet();
                set.add(bundleTask);
                return set;
            }
        };
    }

    private static Project getArtifactProject(Project project, ResolvedDependency unResolvedArtifact) {
        for (Project p : project.getRootProject().getAllprojects()) {
            if (unResolvedArtifact.getModuleName().equals(p.getName())) {
                return p;
            }
        }
        return null;
    }
}
