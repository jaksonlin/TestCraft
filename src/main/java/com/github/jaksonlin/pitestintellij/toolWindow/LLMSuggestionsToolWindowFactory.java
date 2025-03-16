package com.github.jaksonlin.pitestintellij.toolWindow;

import com.github.jaksonlin.pitestintellij.ui.LLMSuggestionsUI;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LLMSuggestionsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel toolWindowPanel =createToolWindowPanel(project);
        Content content = ContentFactory.getInstance().createContent(
                toolWindowPanel,
                "",
                true
        );
        toolWindow.getContentManager().addContent(content);
    }

    private JPanel createToolWindowPanel(@NotNull Project project) {
        LLMSuggestionsUI suggestionsPanel = new LLMSuggestionsUI(project);
        return suggestionsPanel.getPanel();
    }
} 