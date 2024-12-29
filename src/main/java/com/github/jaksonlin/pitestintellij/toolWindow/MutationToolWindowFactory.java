package com.github.jaksonlin.pitestintellij.toolWindow;

import com.github.jaksonlin.pitestintellij.ui.MutationToolWindowUI;
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
        JPanel toolWindowPanel = createToolWindowPanel(project);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = new ContentImpl(toolWindowPanel, "", false); // Directly create ContentImpl
        contentManager.addContent(content);
    }

    private JPanel createToolWindowPanel(@NotNull Project project) {
        MutationToolWindowUI mutationToolWindowUI = new MutationToolWindowUI(project);
        return mutationToolWindowUI.getPanel();
    }
}