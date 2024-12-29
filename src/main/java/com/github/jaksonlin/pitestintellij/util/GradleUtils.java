package com.github.jaksonlin.pitestintellij.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GradleUtils {

    public static List<String> getCompilationOutputPaths(Project project) {
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return new ArrayList<>();
        }
        List<String> outputPaths = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
            if (compilerModuleExtension != null) {
                String outputPath = compilerModuleExtension.getCompilerOutputUrl();
                if (outputPath != null) {
                    outputPaths.add(outputPath.replaceFirst("file://", ""));
                }
                String testOutputPath = compilerModuleExtension.getCompilerOutputUrlForTests();
                if (testOutputPath != null) {
                    outputPaths.add(testOutputPath.replaceFirst("file://", ""));
                }
            }
        }
        return outputPaths;
    }

    public static String getUpperModulePath(Project project, Module childModule) {
        String candidateModuleName = "";
        Module candidateModule = null;
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            String moduleName = module.getName();
            if (childModule.getName().contains(moduleName) && !childModule.getName().equals(moduleName)) {
                if (moduleName.length() > candidateModuleName.length()) {
                    candidateModuleName = moduleName;
                    candidateModule = module;
                }
            }
        }
        if (candidateModule != null) {
            VirtualFile[] contentRoots = ModuleRootManager.getInstance(candidateModule).getContentRoots();
            if (contentRoots.length > 0) {
                return contentRoots[0].getPath();
            }
        }
        return "";
    }

    public static List<String> getTestRunDependencies(Project project) {
        Set<String> dependencies = new HashSet<>();

        for (Module module : ModuleManager.getInstance(project).getModules()) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

            // Get all dependencies, including libraries
            for (OrderEntry orderEntry : moduleRootManager.getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry) {
                    VirtualFile[] rootFiles = ((LibraryOrderEntry) orderEntry).getRootFiles(OrderRootType.CLASSES);
                    for (VirtualFile file : rootFiles) {
                        dependencies.add(file.getPath().replaceAll("!/$", ""));
                    }
                } else {
                    Module dependencyModule = orderEntry.getOwnerModule();
                    ModuleRootManager dependencyModuleRootManager = ModuleRootManager.getInstance(dependencyModule);
                    for (OrderEntry dependencyOrderEntry : dependencyModuleRootManager.getOrderEntries()) {
                        if (dependencyOrderEntry instanceof LibraryOrderEntry) {
                            VirtualFile[] rootFiles = ((LibraryOrderEntry) dependencyOrderEntry).getRootFiles(OrderRootType.CLASSES);
                            for (VirtualFile file : rootFiles) {
                                dependencies.add(file.getPath().replaceAll("!/$", ""));
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(dependencies);
    }

    public static List<String> getResourceDirectories(Project project) {
        List<String> testResourceDirectories = new ArrayList<>();
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] contentSourceRoots = projectRootManager.getContentSourceRoots();
        for (VirtualFile vFile : contentSourceRoots) {
            if (vFile.getPath().endsWith("resources")) {
                testResourceDirectories.add(vFile.getPath());
            }
        }
        return testResourceDirectories;
    }
}