package com.github.jaksonlin.testcraft.infrastructure.services.system;

import com.google.common.eventbus.EventBus;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.application.ApplicationManager;

@Service(Service.Level.APP)
public final class EventBusService {
    private final EventBus eventBus;

    public EventBusService() {
        this.eventBus = new EventBus("TestCraftEventBus");
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public static EventBusService getInstance() {
        return ApplicationManager.getApplication().getService(EventBusService.class);
    }
} 