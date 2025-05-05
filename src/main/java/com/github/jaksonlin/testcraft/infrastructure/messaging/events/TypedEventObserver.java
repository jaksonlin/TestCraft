package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.google.common.eventbus.Subscribe;

public abstract class TypedEventObserver<T extends BaseEvent> {
    private final Class<T> eventType;
    private final EventBusService eventBus;

    protected TypedEventObserver(Class<T> eventType) {
        this.eventType = eventType;
        this.eventBus = EventBusService.getInstance();
        this.eventBus.register(this);
    }

    @Subscribe
    public void onEvent(BaseEvent event) {
        if (eventType.isInstance(event)) {
            onTypedEvent(eventType.cast(event));
        }
    }

    protected abstract void onTypedEvent(T event);

    public void unregister() {
        eventBus.unregister(this);
    }
}
