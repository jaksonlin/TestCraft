package com.github.jaksonlin.pitestintellij.viewmodels;

import com.github.jaksonlin.pitestintellij.components.LLMResponsePanel;
import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.intellij.openapi.application.ApplicationManager;

public class LLMSuggestionUIComponentViewModel  {

    private final LLMService llmService;
    

    public LLMSuggestionUIComponentViewModel(LLMResponsePanel mainPanel) {
        this.llmService = ApplicationManager.getApplication().getService(LLMService.class);
        llmService.addObserver(mainPanel);
    }



    public void generateSuggestions(PitestContext context) {
        llmService.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }

}
