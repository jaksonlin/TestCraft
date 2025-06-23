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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


        collectTargetTestClassInfo(getContext().getTestFilePath());
        collectJavaHome(testVirtualFile);
        collectSourceRoots();

        setWorkingDirectory();
        collectResourceDirectories();
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

    private void collectTargetTestClassInfo(String targetTestClassFilePath) {
        try {
            Optional<ClassFileInfo> testClassInfo = javaFileProcessor.getFullyQualifiedName(targetTestClassFilePath);

            if (!testClassInfo.isPresent()) {
                showError("Cannot get fully qualified name for target test class");
                throw new IllegalStateException("Cannot get fully qualified name for target test class");
            }
            getContext().setFullyQualifiedTargetTestClassName(testClassInfo.get().getFullyQualifiedName());
            getContext().setIsJunit5(testClassInfo.get().getImports().contains("org.junit.jupiter.api.Test"));
        } catch (IOException e) {
            showError("Error getting fully qualified name for target test class: " + e.getMessage());
            throw new IllegalStateException("Error getting fully qualified name for target test class", e);
        }
    }

    private void collectJavaHome(VirtualFile testVirtualFile) {
        ReadAction.run(() -> {
            Module projectModule = ProjectRootManager.getInstance(getProject()).getFileIndex().getModuleForFile(testVirtualFile);
            if (projectModule == null) {
                showError("Cannot find module for test file");
                throw new IllegalStateException("Cannot find module for test file");
            }

            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(projectModule);
            if (moduleRootManager.getSdk() != null) {
                getContext().setJavaHome(moduleRootManager.getSdk().getHomePath());
                getContext().setJavaVersion(moduleRootManager.getSdk().getVersionString());
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

    private String findToolsJarForJDK8() {
        String javaHome = getContext().getJavaHome();
        File javaHomeFile = new File(javaHome);
        File toolsJarFile = new File(javaHomeFile, "lib/tools.jar");
        return toolsJarFile.getAbsolutePath();
    }

    private void collectResourceDirectories() {
        List<String> resourceDirectories = ReadAction.compute(() -> GradleUtils.getResourceDirectories(getProject()));
        String workingDirectory = getContext().getWorkingDirectory();
        // the order of the resource directories is important, the first one should share the same parent directory as working dir
        List<String> newResourceDirectories = new ArrayList<>();
        if (!resourceDirectories.isEmpty()) {
            for (String resourceDirectory : resourceDirectories) {
                if (resourceDirectory.replace("\\", "/").startsWith(workingDirectory.replace("\\", "/"))) {
                    newResourceDirectories.add(resourceDirectory);
                }
            }
            for (String resourceDirectory : resourceDirectories) {
                if (!newResourceDirectories.contains(resourceDirectory)) {
                    newResourceDirectories.add(resourceDirectory);
                }
            }
        }
        getContext().setResourceDirectories(newResourceDirectories);
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
        try {
            Optional<ClassFileInfo> classInfo = javaFileProcessor.getFullyQualifiedName(targetClassInfo.getFile().toString());
            if (!classInfo.isPresent()) {
                showError("Cannot get fully qualified name for target class");
                throw new IllegalStateException("Cannot get fully qualified name for target class");
            }
            getContext().setTargetClassFullyQualifiedName(classInfo.get().getFullyQualifiedName());
            getContext().setTargetClassPackageName(classInfo.get().getPackageName());
            getContext().setTargetClassName(classInfo.get().getClassName());
            getContext().setTargetClassSourceRoot(targetClassInfo.getSourceRoot().toString());
            getContext().setTargetClassFilePath(targetClassInfo.getFile().normalize().toString().replace("\\", "/"));
        } catch (IOException e) {
            showError("Error getting fully qualified name for target class: " + e.getMessage());
            throw new IllegalStateException("Error getting fully qualified name for target class", e);
        }
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

    private List<String> getJunit5PitestPluginJars(List<String> testDependencies) {
        // use regex to match version string like "junit-jupiter-5.7.0.jar" or "junit-jupiter-5.8.1.jar"
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+)\\.jar");
        for (String dependency : testDependencies) {
            if (!dependency.contains("junit-jupiter-")) {
                continue;
            }
            String fileName = dependency.substring(dependency.lastIndexOf(File.separator) + 1);
            Matcher matcher = pattern.matcher(fileName);
            if (!matcher.find()) {
                continue;
            }
            String version = matcher.group(1);
            String[] versionParts = version.split("\\.");
            if (versionParts.length < 3) {
                continue;
            }
            int middleVersionInt = Integer.parseInt(versionParts[1]);
            if (middleVersionInt >= 7 && middleVersionInt <= 11) {
                return getPitestJunit5PluginFile("junit-platform-launcher-1.9.2.jar");
            } else if (middleVersionInt == 12) {
                return getPitestJunit5PluginFile("junit-platform-launcher-1.12.2.jar");
            } else if (middleVersionInt == 13) {
                return getPitestJunit5PluginFile("junit-platform-launcher-1.13.0.jar");
            } else {
                continue;
            }
            
        }
        throw new IllegalStateException("Cannot find JUnit 5 version in dependency: " + testDependencies);
    }

    private List<String> getPitestJunit5PluginFile(String junitPlatformLauncherJar) {
        String pluginLibDir = ReadAction.compute(() -> PathManager.getPluginsPath() + "/TestCraft-Pro/lib");
        
        List<String> junit5PitestPluginJars = new ArrayList<>();
        // check if the jar file exists in the plugin lib directory
        File junitPlatformLauncherFile = new File(pluginLibDir, junitPlatformLauncherJar);
        if (junitPlatformLauncherFile.exists()) {
            junit5PitestPluginJars.add(junitPlatformLauncherFile.getAbsolutePath());
            
        } else {
            showError("Cannot find JUnit Platform Launcher jar: " + junitPlatformLauncherJar);
            throw new IllegalStateException("Cannot find JUnit Platform Launcher jar: " + junitPlatformLauncherJar);
        }
        File apiGuardianFile = new File(pluginLibDir, "apiguardian-api-1.1.2.jar");
        if (apiGuardianFile.exists()) {
            junit5PitestPluginJars.add(apiGuardianFile.getAbsolutePath());
        } else {
            showError("Cannot find API Guardian jar: " + "apiguardian-api-1.1.2.jar");
            throw new IllegalStateException("Cannot find API Guardian jar: " + "apiguardian-api-1.1.2.jar");
        }
        File pitestJunit5PluginFile = new File(pluginLibDir, "pitest-junit5-plugin-1.2.2.jar");
        if (pitestJunit5PluginFile.exists()) {
            junit5PitestPluginJars.add(pitestJunit5PluginFile.getAbsolutePath());
        } else {
            showError("Cannot find PITest JUnit 5 plugin jar: " + "pitest-junit5-plugin-1.2.2.jar");
            throw new IllegalStateException("Cannot find PITest JUnit 5 plugin jar: " + "pitest-junit5-plugin-1.2.2.jar");
        }
        return junit5PitestPluginJars;
    }

    private boolean matchesAnyPattern(String fileName, List<String> patterns) {
        for (String pattern : patterns) {
            // Convert wildcard to regex
            if (fileName.matches(pattern)) {
                return true;
            }
        }
        return false;
    }
    private void sortTestDependencies(List<String> testDependencies) {
        // sort the test dependencies by the order of the dependencies
        String dependencyDirectoriesOrder = MutationConfigService.getInstance().getDependencyDirectoriesOrder();
        String[] dependencyDirectories = dependencyDirectoriesOrder.split(";");
        List<String> firstLoadDependentJarsPatterns = MutationConfigService.getInstance().getFirstLoadDependentJarsPatterns();
       
        List<String> sortedDependencies = new ArrayList<>();
        List<String> remainingDependencies = new ArrayList<>(testDependencies);

        // 1. Add first-load dependencies
        for (String dependency : new ArrayList<>(remainingDependencies)) {
            String fileName = new File(dependency).getName();
            if (matchesAnyPattern(fileName, firstLoadDependentJarsPatterns)) {
                sortedDependencies.add(dependency);
                remainingDependencies.remove(dependency);
            }
        }

        // 2. Add dependencies in the order of the dependency directories
        for (String dependencyDirectory : dependencyDirectories) {
            for (String dependency : new ArrayList<>(remainingDependencies)) {
                String dirName = new File(dependency).getParent();
                if (dirName != null && dirName.endsWith(dependencyDirectory)) {
                    // Check if the dependency is in the current directory
                    // If so, add it to the sorted dependencies
                    // and remove it from the remaining dependencies
                    sortedDependencies.add(dependency);
                    remainingDependencies.remove(dependency);
                }
            }
        }
        // 3. Add remaining dependencies
        sortedDependencies.addAll(remainingDependencies);
        // 4. update the test dependencies
        testDependencies.clear();
        testDependencies.addAll(sortedDependencies);
    }

    private void collectClassPathFileForPitest(String reportDirectory, String targetPackageName, List<String> resourceDirectories) {
        String classPathFileContent = ReadAction.compute(() -> {
            // class file output path
            List<String> classpath = GradleUtils.getCompilationOutputPaths(getProject());
            // external jars
            List<String> testDependencies = GradleUtils.getTestRunDependencies(getProject());
            // sort them by the orders
            sortTestDependencies(testDependencies);
            // 0. add the class file output path
            List<String> allDependencies = new ArrayList<>(classpath);
            // 1. add the resource directories
            if (resourceDirectories != null) {
                allDependencies.addAll(resourceDirectories);
            }
            // 2. add the external jars
            allDependencies.addAll(testDependencies);
            // 3. add the junit 5 pitest plugin jars
            if (getContext().getIsJunit5()) {
                allDependencies.addAll(getJunit5PitestPluginJars(testDependencies));
            }
            // 4. add the tools.jar for JDK 8
            if (getContext().getJavaVersion().contains("1.8.")) {
                allDependencies.add(findToolsJarForJDK8());
            }
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
                    String fileName = file.getName();
                    if (fileName.startsWith("pitest-command")
                            || fileName.startsWith("pitest-entry")
                            || fileName.startsWith("pitest-testcraft-pro")
                            || file.getName().startsWith("commons")) {
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
