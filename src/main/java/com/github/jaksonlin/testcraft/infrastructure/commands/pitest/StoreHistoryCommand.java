package com.github.jaksonlin.testcraft.infrastructure.commands.pitest;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.openapi.application.ApplicationManager;
public class StoreHistoryCommand extends PitestCommand {

    public StoreHistoryCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        runHistoryManager.saveRunHistory(getContext());
        if (getContext().getProcessResult().getExitCode() == 0)
        ApplicationManager.getApplication().invokeLater(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(this.getProject()).getToolWindow("TestCraft");
            if (toolWindow != null) {
                toolWindow.show();
                toolWindow.getContentManager().setSelectedContent(
                    toolWindow.getContentManager().findContent(I18nService.getInstance().message("toolwindow.mutation.tab.name"))
                );
            }
        });
    }
}
