package com.github.jaksonlin.testcraft.messaging.observers;

public interface BasicEventObserver {
    void onEventHappen(String eventName, Object eventObj);
}
