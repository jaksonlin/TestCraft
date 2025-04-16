package com.github.jaksonlin.pitestintellij.observers;

public interface LLMMessageObserver {
    void onLLMMessageResponse(Object eventObj);
}
