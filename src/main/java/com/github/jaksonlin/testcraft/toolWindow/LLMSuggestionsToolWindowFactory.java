package com.github.jaksonlin.testcraft.toolWindow;

import com.github.jaksonlin.testcraft.components.LLMSuggestionUIComponent;
import com.github.jaksonlin.testcraft.services.LLMService;
import com.github.jaksonlin.testcraft.services.RunHistoryManagerService;
import com.intellij.openapi.application.ApplicationManager;
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
        LLMService llmService = ApplicationManager.getApplication().getService(LLMService.class);
        LLMSuggestionUIComponent uiComponent = new LLMSuggestionUIComponent(llmService);
        // add the uiComponent to the toolWindow through the content manager
        JPanel toolWindowPanel = uiComponent.getPanel();
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = new ContentImpl(toolWindowPanel, "TestCraft - LLM Suggestions Tool Window", false); // Directly create ContentImpl
        contentManager.addContent(content);
        // register the uiComponent to the runHistoryManagerService to sync the run history
        RunHistoryManagerService runHistoryManagerService = project.getService(RunHistoryManagerService.class);
        runHistoryManagerService.addObserver(uiComponent);
        
    }
} 