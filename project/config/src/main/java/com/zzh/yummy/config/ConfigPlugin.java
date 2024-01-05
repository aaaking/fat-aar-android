package com.zzh.yummy.config;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
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
    public void apply(Project project) {
    }
}