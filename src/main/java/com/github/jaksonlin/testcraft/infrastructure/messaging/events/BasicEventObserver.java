package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.BaseEvent;
import com.google.common.eventbus.Subscribe;

public abstract class BasicEventObserver {
    protected BasicEventObserver() {
    }

    @Subscribe
    public void onEvent(BaseEvent event) {
        onEventHappen(event.getEventType(), event.getPayload());
    }

    public abstract void onEventHappen(String eventName, Object eventObj);
}
