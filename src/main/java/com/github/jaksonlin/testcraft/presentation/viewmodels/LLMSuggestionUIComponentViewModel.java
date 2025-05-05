package com.github.jaksonlin.testcraft.presentation.viewmodels;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.BasicEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ChatEvent;
import com.github.jaksonlin.testcraft.infrastructure.services.config.LLMConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;


public class LLMSuggestionUIComponentViewModel extends BasicEventObserver {
    private final EventBusService eventBusService = EventBusService.getInstance();
    private final LLMConfigService llmConfigService = LLMConfigService.getInstance();
    public LLMSuggestionUIComponentViewModel() {
        eventBusService.register(this);
    }

    public void propagateConfigChange() {
        llmConfigService.propagateConfigChange();
    }

    public void copyChat() {
        String chatHistory = llmConfigService.getChatHistory();
        // when chatHistory is empty, use the lastDryRunPrompt
        if (chatHistory.isEmpty()) {
            chatHistory = lastDryRunPrompt;
        }
        eventBusService.post(new ChatEvent(ChatEvent.COPY_CHAT_RESPONSE, chatHistory));
    }



    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        // Handle events from LLMService/UIComponent if needed
        switch (eventName) {
            default:
                // if have chat response, stop loading
                if (eventName.contains("CHAT_RESPONSE:")) {
                    eventBusService.post(new ChatEvent(ChatEvent.STOP_LOADING, null));
                }
                eventBusService.post(new ChatEvent(eventName, eventObj));
        }
    }


    public void generateSuggestions(PitestContext context) {
        eventBusService.post(new ChatEvent(ChatEvent.START_LOADING, null));
        llmConfigService.generateUnittestRequest(context.getTestFilePath(), context.getTargetClassFilePath(), context.getMutationResults());
    }

    private String lastDryRunPrompt = "";

    public void dryRunGetPrompt(PitestContext context) {
        lastDryRunPrompt = llmConfigService.dryRunGetPrompt(context.getFullyQualifiedTargetTestClassName(), context.getTargetClassFullyQualifiedName(), context.getMutationResults());
        eventBusService.post(new ChatEvent(ChatEvent.DRY_RUN_PROMPT, lastDryRunPrompt));
    }

    public void handleChatMessage(String message) {
        llmConfigService.handleChatMessage(message);
    }
}
