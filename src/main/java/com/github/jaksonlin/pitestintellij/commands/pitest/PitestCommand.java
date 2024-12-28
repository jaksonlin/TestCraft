package com.github.jaksonlin.pitestintellij.commands.pitest;

import com.github.jaksonlin.pitestintellij.commands.CommandCancellationException;
import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.services.RunHistoryManager;
import com.github.jaksonlin.pitestintellij.ui.PitestOutputDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PitestCommand {
    private final Project project;
    private final PitestContext context;
    protected final RunHistoryManager runHistoryManager;

    public PitestCommand(Project project, PitestContext context) {
        this.project = project;
        this.context = context;
        this.runHistoryManager = project.getService(RunHistoryManager.class);
    }

    public abstract void execute();

    protected Project getProject() {
        return project;
    }

    protected PitestContext getContext() {
        return context;
    }

    protected String showInputDialog(String message, String title) {
        AtomicReference<String> result = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            result.set(Messages.showInputDialog(project, message, title, Messages.getQuestionIcon()));
        }, ModalityState.defaultModalityState());
        return result.get();
    }

    protected void showOutput(String output, String title) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ApplicationManager.getApplication().invokeLater(() -> {
            PitestOutputDialog dialog = new PitestOutputDialog(project, output, title);
            dialog.show();
            if (dialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
                future.completeExceptionally(new CommandCancellationException("User cancelled the dialog"));
            } else {
                future.complete(null);
            }
        });
        try {
            future.get();
        } catch (Exception e) {
            // Handle the exception if needed
        }
    }

    protected void showError(String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String contextState = PitestContext.dumpPitestContext(context);
            String messageWithContextState = message + "\n\n" + contextState;
            Messages.showErrorDialog(project, messageWithContextState, "Pitest Error");
        });
    }
}
