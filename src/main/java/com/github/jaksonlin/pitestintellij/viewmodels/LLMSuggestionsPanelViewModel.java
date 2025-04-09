package com.github.jaksonlin.pitestintellij.viewmodels;

import com.github.jaksonlin.pitestintellij.components.LLMResponsePanel;
import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.mediators.ILLMChatMediator;
import com.github.jaksonlin.pitestintellij.mediators.ILLMChatUI;
import com.github.jaksonlin.pitestintellij.mediators.LLMChatMediatorImpl;
import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class LLMSuggestionsPanelViewModel implements ILLMChatUI {

    private final LLMService llmService;
    private final ILLMChatMediator llmChatMediator = new LLMChatMediatorImpl();
    private final LLMResponsePanel responsePanel;

    public LLMSuggestionsPanelViewModel(Project project, LLMResponsePanel mainPanel) {
        this.llmService = project.getService(LLMService.class);
        this.responsePanel = mainPanel;
        llmService.addObserver(mainPanel);
        llmChatMediator.register(this);
    }

    public void generateSuggestions(PitestContext context) {
        responsePanel.startLoading();
        llmChatMediator.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }

    @Override
    public void updateChatResponse(String chatResponse) {
        ApplicationManager.getApplication().invokeLater(() -> {
            responsePanel.stopLoading();
            llmService.notifyLLMResponse(chatResponse);
        });
    }
}
