package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

public class ItemSelectEvent extends BaseEvent {
    public static final String ITEM_SELECT_EVENT_TYPE_OPEN = "ITEM_SELECT_EVENT_TYPE_OPEN";
    public static final String ITEM_SELECT_EVENT_TYPE_SELECT_ITEMS = "ITEM_SELECT_EVENT_TYPE_SELECT_ITEMS";

    public ItemSelectEvent(String eventType, Object payload) {
        super(eventType, payload);
    }
}
