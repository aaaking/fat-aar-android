package com.zzh.yummy.config;

import static org.codehaus.groovy.runtime.ResourceGroovyMethods.getText;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * author: zhouzhihui
 * created on: 2024/1/6 03:39
 * description:
 */
public class FatUtils {
    private static Project sProject;

    public static void attach(Project p) {
        sProject = p;
    }

    public static void logError(String msg) {
        sProject.getLogger().error("zzh yummy-aar " + msg);
    }

    @Deprecated
    public static void logInfo(String msg) {
        sProject.getLogger().info("zzh yummy-aar " + msg);
    }

    public static void logAnytime(String msg) {
        DefaultGroovyMethods.println(sProject, "zzh yummy-aar " + msg);
    }

    public static void showDir(int indent, File file) throws IOException {
        for (int i = 0; i < indent; i++)
            System.out.print('-');
        System.out.println(file.getName() + " " + file.length());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++)
                showDir(indent + 4, files[i]);
        }
    }

    public static void deleteEmptyDir(final File file) {
        for (int i = 0; i < file.listFiles().length; i++) {
            File x = file.listFiles()[i];
            if (x.isDirectory()) {
                if (x.listFiles().length == 0) {
                    x.delete();
                } else {
                    deleteEmptyDir(x);
                    if (x.listFiles().length == 0) {
                        x.delete();
                    }
                }
            }
        }
    }

    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }

        String[] version1 = v1.split("-");
        String[] version2 = v2.split("-");
        String[] version1Array = version1[0].split("[._]");
        String[] version2Array = version2[0].split("[._]");

        String preRelease1 = new String();
        String preRelease2 = new String();
        if (version1.length > 1) {
            preRelease1 = version1[1];
        }
        if (version2.length > 1) {
            preRelease2 = version2[1];
        }

        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            // compare pre-release
            if (!preRelease1.isEmpty() && preRelease2.isEmpty()) {
                return -1;
            } else if (preRelease1.isEmpty() && !preRelease2.isEmpty()) {
                return 1;
            } else if (!preRelease1.isEmpty() && !preRelease2.isEmpty()) {
                int preReleaseDiff = preRelease1.compareTo(preRelease2);
                if (preReleaseDiff > 0) {
                    return 1;
                } else if (preReleaseDiff < 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    public static String formatDataSize(long size) {
        String result;
        if (size < 1024) {
            result = size + "Byte";
        } else if (size < (1024 * 1024)) {
            result = String.format("%.0fK", size / 1024);
        } else if (size < 1024 * 1024 * 1024) {
            result = String.format("%.2fM", size / (1024 * 1024.0));
        } else {
            result = String.format("%.2fG", size / (1024 * 1024 * 1024.0));
        }
        return result;
    }

    public static void mergeFiles(List<File> inputFiles, File output) {
        if (inputFiles == null) {
            return;
        }
        // filter out any non-existent files
        Supplier<Collection<File>> supplier = () -> new ArrayList<>();
        List<File> existingFiles = inputFiles.stream().filter(f -> f.exists()).collect(Collectors.toList());

        // no input? done.
        if (existingFiles.isEmpty()) {
            return;
        }

        try {
            if (!output.exists()) {
                output.createNewFile();
            }
            // otherwise put all the files together append to output file
            for (File file : existingFiles) {
                ResourceGroovyMethods.append(output, getText(file, "UTF-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkLibraryPlugin() {
        // if (!sProject.getPlugins().hasPlugin("com.android.library")) {
        //     throw new ProjectConfigurationException("fat-aar-plugin must be applied in project that" + " has android library plugin!", new Throwable());
        // }
        return sProject.getPlugins().hasPlugin("com.android.library");
    }

    public static String capitalize(String s) {
        if (s == null || s.length() < 1) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
