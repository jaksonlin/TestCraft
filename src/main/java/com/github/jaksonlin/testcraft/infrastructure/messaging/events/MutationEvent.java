package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public class MutationEvent extends BaseEvent {
    public static final String MUTATION_RESULT = "MUTATION_RESULT";

    public MutationEvent(String eventType, Object payload) {
        super(eventType, payload);
    }
    
    
}
