package com.github.jaksonlin.pitestintellij.viewmodels;

import com.github.jaksonlin.pitestintellij.components.LLMResponsePanel;
import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;

public class LLMSuggestionsPanelViewModel  implements BasicEventObserver {

    private final LLMService llmService;
    
    private final LLMResponsePanel responsePanel;

    public LLMSuggestionsPanelViewModel(Project project, LLMResponsePanel mainPanel) {
        this.llmService = ApplicationManager.getApplication().getService(LLMService.class);
        this.responsePanel = mainPanel;
        llmService.addObserver(this);
        
    }

    public void generateSuggestions(PitestContext context) {
        responsePanel.startLoading();
        llmService.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }


    @Override
    public void onEventHappen(Object eventObj) {
        ApplicationManager.getApplication().invokeLater(() -> {
            responsePanel.stopLoading();
            responsePanel.updateContent(eventObj.toString());
        });
    }
}
