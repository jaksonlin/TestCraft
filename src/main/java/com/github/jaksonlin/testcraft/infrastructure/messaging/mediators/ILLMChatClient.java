package com.github.jaksonlin.testcraft.infrastructure.messaging.mediators;


public interface ILLMChatClient {
    void updateChatResponse(String responseType,String chatResponse);
}
