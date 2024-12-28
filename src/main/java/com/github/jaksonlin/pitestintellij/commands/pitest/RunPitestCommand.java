package com.github.jaksonlin.pitestintellij.commands.pitest;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.util.ProcessExecutor;
import com.github.jaksonlin.pitestintellij.util.ProcessResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class RunPitestCommand extends PitestCommand {
    private static final Logger log = Logger.getInstance(RunPitestCommand.class);

    public RunPitestCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        List<String> command = getContext().getCommand();
        if (command == null || command.isEmpty()) {
            throw new IllegalStateException("Pitest command not set");
        }

        log.info("Run pitest with command: " + String.join(" ", command));
        try {
            File commandFile = new File(Paths.get(getContext().getClasspathFileDirectory(), "command.txt").toString());
            java.nio.file.Files.write(commandFile.toPath(), String.join(" ", command).getBytes());
        } catch (IOException e) {
            log.warn("Error writing command to file: " + e.getMessage());
            // Not throwing exception here as it's not critical for the process execution
        }

        ProcessResult processResult = ProcessExecutor.executeProcess(command);
        getContext().setProcessResult(processResult);
    }
}
