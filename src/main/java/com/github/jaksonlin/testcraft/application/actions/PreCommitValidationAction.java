package com.github.jaksonlin.testcraft.application.actions;

import com.github.jaksonlin.testcraft.infrastructure.commands.testscan.UnittestFileBatchScanCommand;
import com.github.jaksonlin.testcraft.infrastructure.services.business.RunHistoryManagerService;
import com.github.jaksonlin.testcraft.infrastructure.services.config.InvalidTestCaseConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PreCommitValidationAction extends CheckinHandlerFactory {
    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel checkinProjectPanel, @NotNull CommitContext commitContext) {
        return null;
    }
//    private final InvalidTestCaseConfigService invalidTestCaseConfigService;
//    private final RunHistoryManagerService runHistoryManager;
//
//    public PreCommitValidationAction() {
//        this.invalidTestCaseConfigService = ApplicationManager.getApplication().getService(InvalidTestCaseConfigService.class);
//        this.runHistoryManager = RunHistoryManagerService.getInstance();
//    }
//
//    @Override
//    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel checkinProjectPanel, @NotNull CommitContext commitContext) {
//
//        return new CheckinHandler() {
//            @Override
//            public ReturnResult beforeCheckin() {
//                try {
//                    if (!invalidTestCaseConfigService.isEnable()) {
//                        return ReturnResult.COMMIT;
//                    }
//
//                    Project project = checkinProjectPanel.getProject();
//                    if (project == null) {
//                        return ReturnResult.COMMIT;
//                    }
//
//                    Collection<VirtualFile> files = checkinProjectPanel.getVirtualFiles();
//                    if (files == null || files.isEmpty()) {
//                        return ReturnResult.COMMIT;
//                    }
//
//                    // Collect test files
//                    List<PsiFile> testFiles = new ArrayList<>();
//                    PsiManager psiManager = PsiManager.getInstance(project);
//                    for (VirtualFile file : files) {
//                        if (file.getName().endsWith("Test.java")) {
//                            PsiFile psiFile = psiManager.findFile(file);
//                            if (psiFile != null) {
//                                testFiles.add(psiFile);
//                            }
//                        }
//                    }
//
//                    if (!testFiles.isEmpty()) {
//                        // Create a simple data context for the action event
//                        DataContext dataContext = SimpleDataContext.getProjectContext(project);
//                        AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
//                            "PreCommitValidation",
//                            null,
//                            dataContext
//                        );
//
//                        // Run validation on all test files
//                        UnittestFileBatchScanCommand scanCommand = new UnittestFileBatchScanCommand(project, actionEvent);
//
//                        // Create a CompletableFuture to handle the validation result
//                        CompletableFuture<Boolean> validationFuture = new CompletableFuture<>();
//
//                        // Add a listener to handle the validation result
////                        scanCommand.setOnValidationCompleteListener(hasInvalidTests -> {
////                            validationFuture.complete(hasInvalidTests);
////                        });
//
//                        // Execute the validation
//                        scanCommand.execute();
//
//                        try {
//                            // Wait for validation to complete with a timeout
//                            boolean hasInvalidTests = validationFuture.get(30, TimeUnit.SECONDS);
//
//                            if (hasInvalidTests) {
//                                // Show error message and block commit
//                                Messages.showErrorDialog(
//                                    project,
//                                    I18nService.getInstance().message("testscan.commit_blocked_message"),
//                                    I18nService.getInstance().message("testscan.test_case_validation_results")
//                                );
//
//                                return ReturnResult.CANCEL;
//                            }
//                        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
//                            // Handle timeout or other errors
//                            Messages.showErrorDialog(
//                                project,
//                                I18nService.getInstance().message("testscan.validation_timeout_message"),
//                                I18nService.getInstance().message("testscan.test_case_validation_results")
//                            );
//                            return ReturnResult.CANCEL;
//                        }
//                    }
//
//                    return ReturnResult.COMMIT;
//                } catch (Exception e) {
//                    // Handle any unexpected errors
//                    Project project = checkinProjectPanel.getProject();
//                    if (project != null) {
//                        Messages.showErrorDialog(
//                            project,
//                            "An unexpected error occurred during pre-commit validation: " + e.getMessage(),
//                            "Pre-commit Validation Error"
//                        );
//                    }
//                    return ReturnResult.COMMIT;
//                }
//            }
//        };
//    }
} 