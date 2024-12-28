package com.github.jaksonlin.pitestintellij.observers;

import java.util.ArrayList;
import java.util.List;

public class ObserverBase {
    private final List<RunHistoryObserver> observers = new ArrayList<>();

    public void addObserver(RunHistoryObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(RunHistoryObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(Object eventObj) {
        for (RunHistoryObserver observer : observers) {
            observer.onRunHistoryChanged(eventObj);
        }
    }
}
