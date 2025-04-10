package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.mediators.ILLMChatMediator;
import com.github.jaksonlin.pitestintellij.mediators.LLMChatMediatorImpl;
import com.github.jaksonlin.pitestintellij.observers.ObserverBase;
import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import java.util.List;
import com.github.jaksonlin.pitestintellij.mediators.ILLMChatClient;

@Service(Service.Level.APP)
public final class LLMService extends ObserverBase implements ILLMChatClient {
    private static final Logger LOG = Logger.getInstance(LLMService.class);
    private final ILLMChatMediator llmChatMediator = new LLMChatMediatorImpl();

    public LLMService() {
        llmChatMediator.register(this);
    }

    public void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutationList) {
        llmChatMediator.generateUnittestRequest(testCodeFile, sourceCodeFile, mutationList);
    }

    // the mediator is dedicated to talk to the LLM, and when the response is ready, the service will propagate the response to the observers
    // so that on the UI layer, this is the place to notify all the observers
    // the mediator is not responsible for the UI, so it does not know the UI is a Swing UI
    @Override
    public void updateChatResponse(String chatResponse) {
        super.notifyObservers(chatResponse);
    }

}