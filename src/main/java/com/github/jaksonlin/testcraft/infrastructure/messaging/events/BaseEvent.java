package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public abstract class BaseEvent {
    private final String eventType;
    private final Object payload;

    protected BaseEvent(String eventType, Object payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "eventType='" + eventType + '\'' +
                ", payload=" + payload +
                '}';
    }
} 