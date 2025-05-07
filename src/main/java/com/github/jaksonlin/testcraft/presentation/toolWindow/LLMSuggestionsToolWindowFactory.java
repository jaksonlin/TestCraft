package com.github.jaksonlin.testcraft.presentation.toolWindow;

import com.github.jaksonlin.testcraft.presentation.components.llmchat.LLMSuggestionUIComponent;
import com.github.jaksonlin.testcraft.infrastructure.services.business.RunHistoryManagerService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;

import javax.swing.JPanel;

public class LLMSuggestionsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // create a new LLMSuggestionUIComponent
        LLMSuggestionUIComponent uiComponent = new LLMSuggestionUIComponent();
        // add the uiComponent to the toolWindow through the content manager
        JPanel toolWindowPanel = uiComponent.getPanel();
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = new ContentImpl(toolWindowPanel, "TestCraft - LLM Suggestions Tool Window", false); // Directly create ContentImpl
        contentManager.addContent(content);
        // register the uiComponent to the runHistoryManagerService to sync the run history
        RunHistoryManagerService.getInstance().addObserver(uiComponent);
        
    }
} 