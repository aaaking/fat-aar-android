package com.zzh.yummy.config;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;

/**
 * author: zhouzhihui
 * created on: 2024/1/6 05:22
 * description:
 */
public class EmbedResolutionListener implements DependencyResolutionListener {

    private final Project project;

    private final Configuration configuration;

    private final String compileOnlyConfigName;

    EmbedResolutionListener(Project p, Configuration configuration) {
        this.project = p;
        this.configuration = configuration;
        String prefix = getConfigNamePrefix(configuration.getName());
        if (prefix != null) {
            this.compileOnlyConfigName = prefix + "CompileOnly";
        } else {
            this.compileOnlyConfigName = "compileOnly";
        }
    }

    private String getConfigNamePrefix(String configurationName) {
        if (configurationName.endsWith(ConfigPlugin.CONFIG_SUFFIX)) {
            return configurationName.substring(0, configuration.getName().length() - ConfigPlugin.CONFIG_SUFFIX.length());
        } else {
            return null;
        }
    }

    @Override
    public void beforeResolve(ResolvableDependencies resolvableDependencies) {
        for (Dependency dependency : configuration.getDependencies()) {
            if (dependency instanceof DefaultProjectDependency) {
                if (((DefaultProjectDependency) dependency).getTargetConfiguration() == null) {
                    ((DefaultProjectDependency) dependency).setTargetConfiguration("default");
                }
                // support that the module can be indexed in Android Studio 4.0.0
                DefaultProjectDependency dependencyClone = (DefaultProjectDependency) dependency.copy();
                dependencyClone.setTargetConfiguration(null);
                // The purpose is to support the code hints
                project.getDependencies().add(compileOnlyConfigName, dependencyClone);
            } else {
                // The purpose is to support the code hints
                project.getDependencies().add(compileOnlyConfigName, dependency);
            }
        }


        project.getGradle().removeListener(this);
    }

    @Override
    public void afterResolve(ResolvableDependencies resolvableDependencies) {
    }
}