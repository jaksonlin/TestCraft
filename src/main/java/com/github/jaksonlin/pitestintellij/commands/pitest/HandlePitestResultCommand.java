package com.github.jaksonlin.pitestintellij.commands.pitest;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.util.ProcessResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.github.jaksonlin.pitestintellij.ui.PitestOutputDialog;

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
                if (reportFile.exists()) {
                    showOutputWithReportButton(result.getOutput(), "Pitest Output", reportFile);
                } else {
                    showOutput(result.getOutput(), "Pitest Output");
                    showError("Report file not found: " + reportFile.getAbsolutePath());
                }
            }
        } else if (exitCode == -1) {
            showOutput("Error running Pitest:\n\n" + result.getErrorOutput(), "Pitest Error");
        } else {
            showOutput("Pitest exited with code " + exitCode + ":\n\n" + result.getErrorOutput(), "Pitest Error");
        }
    }

    private void showOutputWithReportButton(String output, String title, File reportFile) {
        ApplicationManager.getApplication().invokeLater(() -> {
            new PitestOutputDialog(getProject(), output, title, reportFile).show();
        });
    }
}
