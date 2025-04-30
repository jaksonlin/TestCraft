package com.github.jaksonlin.testcraft.actions;

import com.github.jaksonlin.testcraft.commands.testscan.UnittestFileBatchScanCommand;
import com.github.jaksonlin.testcraft.commands.testscan.UnittestFileInspectorCommand;
import com.github.jaksonlin.testcraft.context.CaseCheckContext;
import com.github.jaksonlin.testcraft.services.InvalidTestCaseConfigService;
import com.intellij.codeInspection.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.CommonDataKeys;

import java.util.*;

public class CheckInvalidTestCasesAction extends AnAction {


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