package com.github.jaksonlin.pitestintellij.viewmodels;

import com.github.jaksonlin.pitestintellij.components.LLMResponsePanel;
import com.github.jaksonlin.pitestintellij.mediators.ILLMChatMediator;
import com.github.jaksonlin.pitestintellij.mediators.ILLMChatUI;
import com.github.jaksonlin.pitestintellij.mediators.LLMChatMediatorImpl;
import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.JPanel;

public class LLMSuggestionsPanelViewModel implements ILLMChatUI {

    private final LLMService llmService;
    private final ILLMChatMediator llmChatMediator = new LLMChatMediatorImpl();


    public LLMSuggestionsPanelViewModel(Project project, LLMResponsePanel mainPanel) {
        // 1. register observer to main panel, when the mediator receives the chat response, it will notify the main panel by calling the updateChatResponse method
        this.llmService = project.getService(LLMService.class);
        llmService.addObserver(mainPanel);
        // 2. register this view model to the mediator
        llmChatMediator.register(this);
    }

    @Override
    public void updateChatResponse(String chatResponse) {
        // mediator is running in a different thread, so we need to use invokeLater to update the UI
        ApplicationManager.getApplication().invokeLater(() -> {
            llmService.notifyLLMResponse(chatResponse);
        });
    }
}
