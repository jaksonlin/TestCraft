package com.github.jaksonlin.pitestintellij.toolWindow;

import com.github.jaksonlin.pitestintellij.ui.LLMSuggestionsUI;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;

import javax.swing.*;

public class LLMSuggestionsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel toolWindowPanel =createToolWindowPanel(project);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = new ContentImpl(toolWindowPanel, "TestCraft - LLM Suggestions Tool Window", false); // Directly create ContentImpl
        contentManager.addContent(content);
    }

    private JPanel createToolWindowPanel(@NotNull Project project) {
        LLMSuggestionsUI suggestionsPanel = new LLMSuggestionsUI(project);
        return suggestionsPanel.getPanel();
    }
} 