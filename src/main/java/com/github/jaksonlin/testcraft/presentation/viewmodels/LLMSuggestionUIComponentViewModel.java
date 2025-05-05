package com.github.jaksonlin.testcraft.presentation.viewmodels;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ChatEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.TypedEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.services.config.LLMConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;


public class LLMSuggestionUIComponentViewModel  {
    private final LLMConfigService llmConfigService = LLMConfigService.getInstance();
    private final EventBusService eventBusService = EventBusService.getInstance();

    private final TypedEventObserver<ChatEvent> chatObserver = new TypedEventObserver<ChatEvent>(ChatEvent.class) {
        @Override
        public void onTypedEvent(ChatEvent event) {
            switch (event.getEventType()) {
                case ChatEvent.REQUEST_COPY_CHAT_RESPONSE:
                    String chatHistory = llmConfigService.getChatHistory();
                    // when chatHistory is empty, use the lastDryRunPrompt
                    if (chatHistory.isEmpty()) {
                        chatHistory = lastDryRunPrompt;
                    }
                    eventBusService.post(new ChatEvent(ChatEvent.COPY_CHAT_RESPONSE, chatHistory));
                    break;
                default:
                    break;
            }
        }
    };

    public void propagateConfigChange() {
        llmConfigService.propagateConfigChange();
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
