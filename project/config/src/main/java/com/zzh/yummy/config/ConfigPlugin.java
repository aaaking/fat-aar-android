package com.zzh.yummy.config;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.artifacts.Configuration;

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
        // project.extensions.create(FatAarExtension.NAME, FatAarExtension);
        // createConfigurations();
        // registerTransform();
        // project.afterEvaluate(project1 -> doAfterEvaluate());
    }

    private void doAfterEvaluate() {

    }
}