package com.github.jaksonlin.testcraft.infrastructure.commands.pitest;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

public class BuildPitestCommandCommand extends PitestCommand {

    public BuildPitestCommandCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        String piptestDependencies = getContext().getPitestDependencies();
        if (piptestDependencies == null) {
            throw new IllegalStateException("Pitest dependencies not set");
        }

        String reportDirectory = getContext().getReportDirectory();
        if (reportDirectory == null) {
            throw new IllegalStateException("Report directory not set");
        }

        String classpathFile = getContext().getClasspathFile();
        if (classpathFile == null) {
            throw new IllegalStateException("Classpath file not set");
        }

        String fullyQualifiedTargetTestClassName = getContext().getFullyQualifiedTargetTestClassName();
        if (fullyQualifiedTargetTestClassName == null) {
            throw new IllegalStateException("Fully qualified target class name not set");
        }

        String fullyQualifiedTargetClassName = getContext().getTargetClassFullyQualifiedName();
        if (fullyQualifiedTargetClassName == null) {
            throw new IllegalStateException("Fully qualified target class name not set");
        }

        String targetClassSourceRoot = getContext().getTargetClassSourceRoot();
        if (targetClassSourceRoot == null) {
            throw new IllegalStateException("target class source root not set");
        }

        String javaHome = getContext().getJavaHome();
        if (javaHome == null) {
            throw new IllegalStateException("Java home not set");
        }

        String javaExe = javaHome + "/bin/java";

        List<String> command = new ArrayList<>();
        command.add(javaExe);
        command.add("-cp");
        command.add(piptestDependencies);
        command.add("org.pitest.mutationtest.commandline.MutationCoverageReport");
        command.add("--reportDir");
        command.add(reportDirectory);
        command.add("--targetClasses");
        command.add(fullyQualifiedTargetClassName);
        command.add("--sourceDirs");
        command.add(targetClassSourceRoot);
        command.add("--classPathFile");
        command.add(classpathFile);
        command.add("--targetTests");
        command.add(fullyQualifiedTargetTestClassName);
        command.add("--outputFormats");
        command.add("HTML,XML");
        command.add("--timeoutConst");
        command.add("10000");
        command.add("--threads");
        command.add("4");
        command.add("--verbose");
        command.add("true");
        command.add("--timeoutFactor");
        command.add("2.0");
        command.add("--mutators");
        command.add("STRONGER");
        command.add("--skipFailingTests");

        if (getContext().getMethodsToMutate() != null) {
            command.add("--targetMethods");
            command.add(getContext().getMethodsToMutate());
        }

        getContext().setCommand(command);
    }
}
