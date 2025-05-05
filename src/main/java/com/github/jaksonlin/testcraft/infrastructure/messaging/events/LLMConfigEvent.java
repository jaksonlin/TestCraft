package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public class LLMConfigEvent extends BaseEvent {
    public static final String CONFIG_CHANGE = "CONFIG_CHANGE";
    public static final String CONFIG_CHANGE_COPY_AS_MARKDOWN = "CONFIG_CHANGE:copyAsMarkdown";

    public LLMConfigEvent(String eventType, Object payload) {
        super(eventType, payload);
    }
}
