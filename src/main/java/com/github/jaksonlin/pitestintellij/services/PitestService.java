package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.commands.CommandCancellationException;
import com.github.jaksonlin.pitestintellij.commands.pitest.*;
import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import com.github.jaksonlin.pitestintellij.MyBundle;
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

@Service(Service.Level.APP)
public final class PitestService {

    public void runPitest(Project targetProject, String testFilePath) {
        PitestContext context = new PitestContext(testFilePath, System.currentTimeMillis());

        List<PitestCommand> commands = Arrays.asList(
                new PrepareEnvironmentCommand(targetProject, context),
                new BuildPitestCommandCommand(targetProject, context),
                new RunPitestCommand(targetProject, context),
                new HandlePitestResultCommand(targetProject, context),
                new StoreHistoryCommand(targetProject, context)
        );

        new Task.Backgroundable(targetProject, "Running pitest", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    for (PitestCommand command : commands) {
                        if (indicator.isCanceled()) {
                            ApplicationManager.getApplication().invokeLater(() ->
                                Messages.showInfoMessage(MyBundle.message("pitest.run.canceled"), MyBundle.message("pitest.run.canceled.title"))
                            );
                            break;
                        }
                        command.execute();
                    }
                } catch (Exception e) {
                    if (e.getCause() instanceof CommandCancellationException) {
                        ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showInfoMessage(MyBundle.message("pitest.run.canceled"), MyBundle.message("pitest.run.canceled.title"))
                        );
                    } else {
                        showErrorDialog(e, context);
                    }
                }
            }
        }.queue();
    }

    protected void showErrorDialog(Exception e, PitestContext context) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        String contextInformation = PitestContext.dumpPitestContext(context);
        String errorMessage = MyBundle.message("error.pitest.general.title") + ": " + e.getMessage() + "; " + contextInformation + "\n" + stackTrace;

        JTextArea textArea = new JTextArea(errorMessage);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(640, 480));

        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showErrorDialog(scrollPane, errorMessage)
        );
    }
}
