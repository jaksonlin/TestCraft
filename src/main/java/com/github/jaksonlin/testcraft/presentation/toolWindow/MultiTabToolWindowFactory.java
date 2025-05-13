package com.github.jaksonlin.testcraft.presentation.toolWindow;

import com.github.jaksonlin.testcraft.presentation.components.system.ToolWindowMainComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MultiTabToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ToolWindowMainComponent multiTabToolWindow = new ToolWindowMainComponent(project, toolWindow);
        ContentManager contentManager = toolWindow.getContentManager();
        JComponent toolWindowPanel = multiTabToolWindow.getContent();
        Content content = new ContentImpl(toolWindowPanel, "", false);
        contentManager.addContent(content);
    }
} 