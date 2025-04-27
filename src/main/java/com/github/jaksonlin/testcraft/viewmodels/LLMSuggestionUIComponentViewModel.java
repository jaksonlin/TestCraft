package com.github.jaksonlin.testcraft.viewmodels;

import com.github.jaksonlin.testcraft.context.PitestContext;
import com.github.jaksonlin.testcraft.observers.BasicEventObserver;
import com.github.jaksonlin.testcraft.observers.ObserverBase;
import com.github.jaksonlin.testcraft.services.LLMService;

public class LLMSuggestionUIComponentViewModel extends ObserverBase implements BasicEventObserver {

    private final LLMService llmService;
    

    public LLMSuggestionUIComponentViewModel(LLMService llmService) {
        this.llmService = llmService;
        llmService.addObserver(this); // as hub to forward events
        this.addObserver(llmService);
    }

    public void propagateConfigChange() {
        llmService.propagateConfigChange();
    }

    public void clearChat() {
        notifyObservers("CLEAR_CHAT", null);
    }

    public void copyChat() {
        String chatHistory = llmService.getChatHistory();
        // when chatHistory is empty, use the lastDryRunPrompt
        if (chatHistory.isEmpty()) {
            chatHistory = lastDryRunPrompt;
        }
        notifyObservers("COPY_CHAT_RESPONSE", chatHistory);
    }



    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        // Handle events from LLMService/UIComponent if needed
        switch (eventName) {
            default:
                // if have chat response, stop loading
                if (eventName.contains("CHAT_RESPONSE:")) {
                    notifyObservers("STOP_LOADING", null);
                }
                notifyObservers(eventName, eventObj);
                break;
        }
    }


    public void generateSuggestions(PitestContext context) {
        notifyObservers("START_LOADING", null);
        llmService.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }

    private String lastDryRunPrompt = "";

    public void dryRunGetPrompt(PitestContext context) {
        lastDryRunPrompt = llmService.dryRunGetPrompt(context.getFullyQualifiedTargetTestClassName(), context.getTargetClassFullyQualifiedName(), context.getMutationResults());
        notifyObservers("DRY_RUN_PROMPT", lastDryRunPrompt);
    }

    public void handleChatMessage(String message) {
        llmService.handleChatMessage(message);
    }
}
