package com.github.jaksonlin.testcraft.presentation.toolWindow;

import com.github.jaksonlin.testcraft.presentation.components.system.ToolWindowMainComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class MultiTabToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ToolWindowMainComponent multiTabToolWindow = new ToolWindowMainComponent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(
            multiTabToolWindow.getContent(),
            "",
            false
        );
        toolWindow.getContentManager().addContent(content);
    }
} 