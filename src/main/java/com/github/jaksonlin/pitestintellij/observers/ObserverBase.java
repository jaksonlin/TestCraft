package com.github.jaksonlin.pitestintellij.observers;

import java.util.ArrayList;
import java.util.List;

public class ObserverBase {
    private final List<BasicEventObserver> observers = new ArrayList<>();

    public void addObserver(BasicEventObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BasicEventObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String eventName, Object eventObj) {
        for (BasicEventObserver observer : observers) {
            observer.onEventHappen(eventName, eventObj);
        }
    }
}
