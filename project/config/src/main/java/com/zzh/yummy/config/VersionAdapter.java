package com.zzh.yummy.config;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.TaskProvider;

/**
 * author: zhouzhihui
 * created on: 2024/1/30 10:41
 * description:
 */
public class VersionAdapter {
    public static TaskProvider<Task> getBundleTaskProvider(Project project, String variantName) throws UnknownTaskException {
        String taskPath = "bundle" + FatUtils.capitalize(variantName);
        TaskProvider bundleTask;
        try {
            bundleTask = project.getTasks().named(taskPath);
        } catch (UnknownTaskException ignored) {
            taskPath += "Aar";
            bundleTask = project.getTasks().named(taskPath);
        }
        return bundleTask;
    }
}
