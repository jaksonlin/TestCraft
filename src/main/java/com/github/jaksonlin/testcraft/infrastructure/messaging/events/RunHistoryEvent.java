package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public class RunHistoryEvent extends BaseEvent {
    public static final String RUN_HISTORY = "RUN_HISTORY";
    public static final String RUN_HISTORY_LIST = "RUN_HISTORY_LIST";

    public RunHistoryEvent(String eventType, Object payload) {
        super(eventType, payload);
    }
}
