package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public class MutationEvent extends BaseEvent {
    public static final String MUTATION_RESULT = "MUTATION_RESULT";
    public static final String MUTATION_EVENT_TYPE_OPEN_NODE = "MUTATION_EVENT_TYPE_OPEN_NODE";

    public MutationEvent(String eventType, Object payload) {
        super(eventType, payload);
    }
    
    
}
