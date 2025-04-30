package com.github.jaksonlin.testcraft.presentation.actions;

import com.github.jaksonlin.testcraft.core.services.PitestService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class RunPitestAction extends AnAction {

    private final PitestService pitestService;

    public RunPitestAction() {
        pitestService = ApplicationManager.getApplication().getService(PitestService.class);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project targetProject = e.getProject();
        if (targetProject == null) {
            return;
        }

        VirtualFile testVirtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (testVirtualFile == null) {
            return;
        }

        pitestService.runPitest(targetProject, testVirtualFile.getPath());
    }
}