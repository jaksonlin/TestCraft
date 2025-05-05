package com.github.jaksonlin.testcraft.infrastructure.commands.pitest;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.github.jaksonlin.testcraft.presentation.components.PitestOutputDialog;
import com.github.jaksonlin.testcraft.util.ProcessResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class HandlePitestResultCommand extends PitestCommand {

    public HandlePitestResultCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        ProcessResult result = getContext().getProcessResult();
        if (result == null) {
            throw new IllegalStateException("Process result not available");
        }

        int exitCode = result.getExitCode();
        if (exitCode == 0) {
            String reportDirectory = getContext().getReportDirectory();
            if (reportDirectory != null) {
                File reportFile = new File(reportDirectory, "index.html");
                StringBuilder combinedOutputSB = getCombinedOutputFromResult(result);
                if (reportFile.exists()) {
                    showOutputWithReportButton(combinedOutputSB.toString(), "Pitest Output", reportFile);
                } else {
                    showOutput(combinedOutputSB.toString(), "Pitest Result");
                    showError("Report file not found: " + reportFile.getAbsolutePath());
                }
            }
        } else {
            StringBuilder errorMessage = new StringBuilder()
                .append("Pitest exited with code ").append(exitCode).append("\n\n")
                .append("This might indicate an abnormal test process termination.\n")
                .append("Common causes for exit code 2 include:\n")
                .append("- System.exit() called in test code\n")
                .append("- Test process configuration issues\n")
                .append("- Memory/resource constraints\n\n");
            if (result.getOutput() != null) {
                errorMessage.append("=== Standard Output ===\n");
                errorMessage.append(result.getOutput());
            }
            if (result.getErrorOutput() != null) {
                errorMessage.append("\n=== Verbose Output ===\n");
                errorMessage.append(result.getErrorOutput());
            }

            showOutput(errorMessage.toString(), "Pitest Error");
        }
    }

    private static @NotNull StringBuilder getCombinedOutputFromResult(ProcessResult result) {
        StringBuilder combinedOutputSB = new StringBuilder();
        if (result.getOutput() != null) {
            combinedOutputSB.append("=== Standard Output ===\n");
            combinedOutputSB.append(result.getOutput());
        }
        if (result.getErrorOutput() != null) {
            combinedOutputSB.append("\n=== Verbose Output ===\n");
            combinedOutputSB.append(result.getErrorOutput());
        }
        return combinedOutputSB;
    }

    private void showOutputWithReportButton(String output, String title, File reportFile) {
        ApplicationManager.getApplication().invokeLater(() -> {
            new PitestOutputDialog(getProject(), output, title, reportFile).show();
        });
    }
}
