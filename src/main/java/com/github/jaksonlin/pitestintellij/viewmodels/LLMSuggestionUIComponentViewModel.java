package com.github.jaksonlin.pitestintellij.viewmodels;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.services.LLMService;

public class LLMSuggestionUIComponentViewModel  {

    private final LLMService llmService;
    

    public LLMSuggestionUIComponentViewModel(LLMService llmService) {
        this.llmService = llmService;
    }



    public void generateSuggestions(PitestContext context) {
        llmService.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }

}
