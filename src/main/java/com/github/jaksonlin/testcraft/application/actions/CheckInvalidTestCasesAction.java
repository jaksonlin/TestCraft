package com.github.jaksonlin.testcraft.application.actions;

import com.github.jaksonlin.testcraft.infrastructure.commands.testscan.UnittestFileBatchScanCommand;
import com.github.jaksonlin.testcraft.infrastructure.services.config.InvalidTestCaseConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class CheckInvalidTestCasesAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText(I18nService.getInstance().message("action.CheckInvalidTestCasesAction.text"));
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        InvalidTestCaseConfigService configService = ApplicationManager.getApplication().getService(InvalidTestCaseConfigService.class);
        if (!configService.isEnable()) {
            Messages.showInfoMessage(
                project,
                "Test case validation is disabled. Please enable it in Settings → TestCraft → Test Case Validation",
                "Test Case Validation Disabled"
            );
            return;
        }

        UnittestFileBatchScanCommand batchScanCommand = new UnittestFileBatchScanCommand(project, e);
        batchScanCommand.execute();
    }





} 