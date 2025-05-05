package com.github.jaksonlin.testcraft.infrastructure.messaging.events;


@FunctionalInterface
public interface EventHandler<T extends BaseEvent> {
    void handle(T event);
}

