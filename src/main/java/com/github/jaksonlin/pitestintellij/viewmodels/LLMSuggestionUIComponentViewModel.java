package com.github.jaksonlin.pitestintellij.viewmodels;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.github.jaksonlin.pitestintellij.observers.ObserverBase;
import com.github.jaksonlin.pitestintellij.services.LLMService;

public class LLMSuggestionUIComponentViewModel extends ObserverBase implements BasicEventObserver {

    private final LLMService llmService;
    

    public LLMSuggestionUIComponentViewModel(LLMService llmService) {
        this.llmService = llmService;
        llmService.addObserver(this); // as hub to forward events
        this.addObserver(llmService);
    }

    public void clearChat() {
        notifyObservers("CLEAR_CHAT", null);
    }

    public void copyChat() {
        String chatHistory = llmService.getChatHistory();
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
        notifyObservers("CLEAR_SUGGESTIONS", null);
        notifyObservers("START_LOADING", null);
        llmService.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }

    public void dryRunGetPrompt(PitestContext context) {
        String prompt = llmService.dryRunGetPrompt(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
        notifyObservers("DRY_RUN_PROMPT", prompt);
    }

    public void handleChatMessage(String message) {
        llmService.handleChatMessage(message);
    }
}
