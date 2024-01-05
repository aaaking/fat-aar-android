package com.zzh.yummy.config;

import com.android.build.api.variant.impl.LibraryVariantImpl;
import com.android.build.gradle.api.LibraryVariant;

import org.gradle.api.Project;

import java.io.File;

/**
 * author: zhouzhihui
 * created on: 2024/1/6 03:57
 * description:
 */
public class DirectoryManager {
    private static final String RE_BUNDLE_FOLDER = "aar_rebundle";

    private static final String INTERMEDIATES_TEMP_FOLDER = "fat-aar";

    private static Project sProject;

    static void attach(Project project) {
        sProject = project;
    }

    static File getReBundleDirectory(LibraryVariantImpl variant) {
        File buildDir = sProject.getBuildDir();
        return sProject.file(buildDir.getAbsolutePath() + "/outputs/" + RE_BUNDLE_FOLDER + "/" + variant.getName());
    }

    static File getRJavaDirectory(LibraryVariant variant) {
        File buildDir = sProject.getBuildDir();
        return sProject.file(buildDir.getAbsolutePath() + "/intermediates/" + INTERMEDIATES_TEMP_FOLDER + "/r/" + variant.getName());
    }

    static File getRClassDirectory(LibraryVariant variant) {
        File buildDir = sProject.getBuildDir();
        return sProject.file(buildDir.getAbsolutePath() + "/intermediates/" + INTERMEDIATES_TEMP_FOLDER + "/r-class/" + variant.getName());
    }

    static File getRJarDirectory(LibraryVariant variant) {
        File buildDir = sProject.getBuildDir();
        return sProject.file(buildDir.getAbsolutePath() + "/outputs/" + RE_BUNDLE_FOLDER + "/" + variant.getName() +"/libs");
    }

    static File getMergeClassDirectory(LibraryVariant variant) {
        File buildDir = sProject.getBuildDir();
        return sProject.file(buildDir.getAbsolutePath() + "/intermediates/" + INTERMEDIATES_TEMP_FOLDER + "/merge_classes/" + variant.getName());
    }

    static File getKotlinMetaDirectory(LibraryVariant variant) {
        File buildDir = sProject.getBuildDir();
        return sProject.file(buildDir.getAbsolutePath() + "/tmp/kotlin-classes/" + variant.getName() + "/META-INF");
    }
}
