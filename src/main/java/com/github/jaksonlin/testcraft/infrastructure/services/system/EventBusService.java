package com.github.jaksonlin.testcraft.infrastructure.services.system;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.BaseEvent;
import com.google.common.eventbus.EventBus;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.application.ApplicationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(Service.Level.APP)
public final class EventBusService {
    private final EventBus eventBus;
    private final Logger logger = LoggerFactory.getLogger(EventBusService.class);

    public EventBusService() {
        this.eventBus = new EventBus("TestCraftEventBus");
    }

    public void register(Object subscriber) {
        try {
            eventBus.register(subscriber);
            logger.debug("Registered subscriber: {}", subscriber.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Failed to register subscriber: " + subscriber, e);
        }
    }

    public void unregister(Object subscriber) {
        try {
            eventBus.unregister(subscriber);
            logger.debug("Unregistered subscriber: {}", subscriber.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Failed to unregister subscriber: " + subscriber, e);
        }
    }

    public void post(Object event) {
        try {
            if (event instanceof BaseEvent) {
                logger.debug("Posting event: {}", ((BaseEvent) event).getEventType());
            }
            eventBus.post(event);
        } catch (Exception e) {
            logger.error("Failed to post event: " + event, e);
        }
    }

    public static EventBusService getInstance() {
        return ApplicationManager.getApplication().getService(EventBusService.class);
    }
} 