package com.github.jaksonlin.testcraft.infrastructure.commands.pitest;

import com.github.jaksonlin.testcraft.infrastructure.commands.CommandCancellationException;
import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.github.jaksonlin.testcraft.util.*;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.github.jaksonlin.testcraft.infrastructure.services.config.MutationConfigService;

public class PrepareEnvironmentCommand extends PitestCommand {
    private final JavaFileProcessor javaFileProcessor = new JavaFileProcessor();

    public PrepareEnvironmentCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        VirtualFile testVirtualFile = ReadAction.compute(() ->
                LocalFileSystem.getInstance().findFileByPath(getContext().getTestFilePath()));
        if (testVirtualFile == null) {
            showError("Cannot find test file");
            throw new IllegalStateException("Cannot find test file");
        }

        collectTargetTestClassName(getContext().getTestFilePath());
        collectJavaInfo(testVirtualFile);
        collectSourceRoots();
        collectResourceDirectories();
        setWorkingDirectory();
        collectMutatorGroup();

        if (getContext().getSourceRoots() != null) {
            collectTargetClassThatWeTest(getContext().getSourceRoots());
        } else {
            showError("Source roots are not collected.");
            throw new IllegalStateException("Source roots are not collected.");
        }
        if (getContext().getTargetClassFullyQualifiedName() != null) {
            prepareReportDirectory(testVirtualFile, getContext().getTargetClassFullyQualifiedName());
        } else {
            showError("Target class fully qualified name is not collected.");
            throw new IllegalStateException("Target class fully qualified name is not collected.");
        }

        if (getContext().getResourceDirectories() != null) {
            setupPitestLibDependencies(getContext().getResourceDirectories());
            if (getContext().getReportDirectory() != null && getContext().getTargetClassPackageName() != null && getContext().getResourceDirectories() != null) {
                collectClassPathFileForPitest(getContext().getReportDirectory(), getContext().getTargetClassPackageName(), getContext().getResourceDirectories());
            } else {
                showError("Report directory, target package name or resource directories are not collected.");
                throw new IllegalStateException("Report directory, target package name or resource directories are not collected.");
            }
        } else {
            showError("Resource directories are not collected.");
            throw new IllegalStateException("Resource directories are not collected.");
        }
    }

    private void collectTargetTestClassName(String targetTestClassFilePath) {
        ClassFileInfo testClassInfo = javaFileProcessor.getFullyQualifiedName(targetTestClassFilePath);

        if (testClassInfo == null) {
            showError("Cannot get fully qualified name for target test class");
            throw new IllegalStateException("Cannot get fully qualified name for target test class");
        }

        getContext().setFullyQualifiedTargetTestClassName(testClassInfo.getFullyQualifiedName());
    }

    private void collectJavaInfo(VirtualFile testVirtualFile) {
        ReadAction.run(() -> {
            Module projectModule = ProjectRootManager.getInstance(getProject()).getFileIndex().getModuleForFile(testVirtualFile);
            if (projectModule == null) {
                showError("Cannot find module for test file");
                throw new IllegalStateException("Cannot find module for test file");
            }

            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(projectModule);
            if (moduleRootManager.getSdk() != null) {
                getContext().setJavaHome(moduleRootManager.getSdk().getHomePath());
            }
        });
        if (getContext().getJavaHome() == null || getContext().getJavaHome().isEmpty()) {
            showError("Cannot find java home");
            throw new IllegalStateException("Cannot find java home");
        }
    }

    private void collectSourceRoots() {
        List<String> sourceRoots = ReadAction.compute(() -> {
            List<String> roots = new ArrayList<>();
            for (Module module : ModuleManager.getInstance(getProject()).getModules()) {
                for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
                    roots.add(Paths.get(contentRoot.getPath()).toString());
                }
            }
            return roots;
        });
        getContext().setSourceRoots(sourceRoots);
    }

    private void setWorkingDirectory() {
        List<String> sourceRoots = getContext().getSourceRoots();

        // get the test file path
        String testFilePathWithoutSrc = getContext().getTestFilePath().split("src/test/java")[0];
        // normalize the test file path
        Path normalizedPath = Paths.get(testFilePathWithoutSrc).normalize();
        // normalized path 
        testFilePathWithoutSrc = normalizedPath.toString();
        // get the source root that contains the test file
        String sourceRoot = null;
        for (String root : sourceRoots) {
            if (testFilePathWithoutSrc.equals(root)) {
                sourceRoot = root;
                break;
            }
        }
        if (sourceRoot == null) {
            showError("Cannot find source root as working directory to run pitest for test file, expected source root: " + testFilePathWithoutSrc);
            throw new IllegalStateException("Cannot find source root for test file");
        }
        // set the working directory to the source root
        getContext().setWorkingDirectory(sourceRoot);
    }

    private void collectResourceDirectories() {
        List<String> resourceDirectories = ReadAction.compute(() -> GradleUtils.getResourceDirectories(getProject()));
        getContext().setResourceDirectories(resourceDirectories);
    }

    private void collectTargetClassThatWeTest(List<String> sourceRoots) {
        String targetClass = showInputDialog(I18nService.getInstance().message("dialog.target.class.message"), I18nService.getInstance().message("dialog.target.class.title"));
        if (targetClass == null || targetClass.isEmpty()) {
            try {
                throw new CommandCancellationException("User cancelled the operation");
            } catch (CommandCancellationException e) {
                throw new RuntimeException(e);
            }
        }
        TargetClassInfo targetClassInfo = FileUtils.findTargetClassFile(sourceRoots, targetClass);
        if (targetClassInfo == null) {
            showError("Cannot find target class file");
            throw new IllegalStateException("Cannot find target class file");
        }
        ClassFileInfo classInfo = javaFileProcessor.getFullyQualifiedName(targetClassInfo.getFile().toString());

        if (classInfo == null) {
            showError("Cannot get fully qualified name for target class");
            throw new IllegalStateException("Cannot get fully qualified name for target class");
        }
        getContext().setTargetClassFullyQualifiedName(classInfo.getFullyQualifiedName());
        getContext().setTargetClassPackageName(classInfo.getPackageName());
        getContext().setTargetClassName(classInfo.getClassName());
        getContext().setTargetClassSourceRoot(targetClassInfo.getSourceRoot().toString());
        getContext().setTargetClassFilePath(targetClassInfo.getFile().normalize().toString().replace("\\", "/"));
    }

    private void prepareReportDirectory(VirtualFile testVirtualFile, String className) {
        String parentModulePath = ReadAction.compute(() -> {
            Module projectModule = ProjectRootManager.getInstance(getProject()).getFileIndex().getModuleForFile(testVirtualFile);
            if (projectModule == null) {
                throw new IllegalStateException("Cannot find module for test file");
            }

            String modulePath = GradleUtils.getUpperModulePath(getProject(), projectModule);
            if (modulePath.isEmpty()) {
                return ModuleRootManager.getInstance(projectModule).getContentRoots()[0].getPath();
            } else {
                return modulePath;
            }
        });

        getContext().setReportDirectory(Paths.get(parentModulePath, "build", "reports", "pitest", className).toString());
        File reportDir = new File(getContext().getReportDirectory());
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
    }

    private void collectClassPathFileForPitest(String reportDirectory, String targetPackageName, List<String> resourceDirectories) {
        String classPathFileContent = ReadAction.compute(() -> {
            List<String> classpath = GradleUtils.getCompilationOutputPaths(getProject());
            List<String> testDependencies = GradleUtils.getTestRunDependencies(getProject());
            List<String> allDependencies = new ArrayList<>(classpath);
            if (resourceDirectories != null) {
                allDependencies.addAll(resourceDirectories);
            }
            allDependencies.addAll(testDependencies);
            return String.join("\n", allDependencies);
        });
        showOutput("Classpath file content: " + classPathFileContent, "Classpath file content");
        getContext().setClasspathFileDirectory(Paths.get(reportDirectory, targetPackageName).toString());
        File classpathDir = new File(getContext().getClasspathFileDirectory());
        if (!classpathDir.exists()) {
            classpathDir.mkdirs();
        }
        getContext().setClasspathFile(Paths.get(getContext().getClasspathFileDirectory(), "classpath.txt").toString());
        try {
            java.nio.file.Files.write(Paths.get(getContext().getClasspathFile()), classPathFileContent.getBytes());
        } catch (java.io.IOException e) {
            showError("Error writing classpath file: " + e.getMessage());
            throw new IllegalStateException("Error writing classpath file", e);
        }
    }

    private void setupPitestLibDependencies(List<String> resourceDirectories) {
        String pluginLibDir = ReadAction.compute(() -> PathManager.getPluginsPath() + "/TestCraft-Pro/lib");
        List<String> dependencies = new ArrayList<>();
        File libDir = new File(pluginLibDir);
        File[] files = libDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".jar")) {
                    if (file.getName().startsWith("pitest") || file.getName().startsWith("commons")) {
                        dependencies.add(file.getAbsolutePath());
                    }
                }
            }
        }
        dependencies.addAll(resourceDirectories);
        if (dependencies.isEmpty()) {
            Messages.showErrorDialog(I18nService.getInstance().message("error.pitest.dependencies"), I18nService.getInstance().message("error.pitest.title"));
            throw new IllegalStateException("Cannot find pitest dependencies");
        }
        getContext().setPitestDependencies(String.join(File.pathSeparator, dependencies));
    }

    private void collectMutatorGroup() {
        getContext().setMutatorGroup(MutationConfigService.getInstance().getMutatorGroup());
    }
}
