package com.github.jaksonlin.testcraft.presentation.toolWindow;

import com.github.jaksonlin.testcraft.presentation.components.mutation.MutationToolWindowUIComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl; // Note the use of ContentImpl
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MutationToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MutationToolWindowUIComponent mutationToolWindowUI = new MutationToolWindowUIComponent(project);
        JPanel toolWindowPanel = mutationToolWindowUI.getPanel();
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = new ContentImpl(toolWindowPanel, "TestCraft - Mutation Tool Window", false); // Directly create ContentImpl
        contentManager.addContent(content);
    }

}