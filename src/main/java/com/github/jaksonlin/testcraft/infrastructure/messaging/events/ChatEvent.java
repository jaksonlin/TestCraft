package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public class ChatEvent extends BaseEvent {
    public static final String CLEAR_CHAT = "CLEAR_CHAT";
    public static final String CHAT_REQUEST = "CHAT_REQUEST";
    public static final String CHAT_RESPONSE = "CHAT_RESPONSE";
    public static final String START_LOADING = "START_LOADING";
    public static final String STOP_LOADING = "STOP_LOADING";
    public static final String COPY_CHAT_RESPONSE = "COPY_CHAT_RESPONSE";
    public static final String CONFIG_CHANGE_COPY_AS_MARKDOWN = "CONFIG_CHANGE:copyAsMarkdown";
    public static final String DRY_RUN_PROMPT = "DRY_RUN_PROMPT";
    public static final String ERROR = "ERROR";

    public ChatEvent(String eventType, Object payload) {
        super(eventType, payload);
    }
} 