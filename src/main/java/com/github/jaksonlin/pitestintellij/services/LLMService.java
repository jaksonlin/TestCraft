package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.mediators.ILLMChatMediator;
import com.github.jaksonlin.pitestintellij.mediators.LLMChatMediatorImpl;
import com.github.jaksonlin.pitestintellij.observers.ObserverBase;
import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.github.jaksonlin.pitestintellij.util.OllamaClient;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.jaksonlin.pitestintellij.mediators.ILLMChatClient;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.PersistentStateComponent;
@Service(Service.Level.APP)
@State(
    name = "com.github.jaksonlin.pitestintellij.services.LLMService",
    storages = @Storage("LLMService.xml")
)
public final class LLMService extends ObserverBase implements ILLMChatClient, PersistentStateComponent<LLMService.State> {
    private static final Logger LOG = Logger.getInstance(LLMService.class);
    private final ILLMChatMediator llmChatMediator = new LLMChatMediatorImpl();

    public LLMService() {
        llmChatMediator.register(this);
    }

    public static class State {
        private String ollamaHost = "localhost";
        private int ollamaPort = 11434;
        private String ollamaModel = "deepseek-r1:32b";
        private int maxTokens = 2000;
        private float temperature = 0.7f;
        private int requestTimeout = 60;  // seconds
        private boolean copyAsMarkdown = false;

        public State() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return ollamaPort == state.ollamaPort &&
                    maxTokens == state.maxTokens &&
                    Float.compare(state.temperature, temperature) == 0 &&
                    requestTimeout == state.requestTimeout &&
                    copyAsMarkdown == state.copyAsMarkdown &&
                    Objects.equals(ollamaHost, state.ollamaHost) &&
                    Objects.equals(ollamaModel, state.ollamaModel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ollamaHost, ollamaPort, ollamaModel, maxTokens, temperature, requestTimeout, copyAsMarkdown);
        }
    }

    private State myState = new State();

    public boolean getCopyAsMarkdown() {
        return myState.copyAsMarkdown;
    }

    public String getOllamaHost() {
        return myState.ollamaHost;
    }

    public int getOllamaPort() {
        return myState.ollamaPort;
    }

    public String getOllamaModel() {
        return myState.ollamaModel;
    }

    public int getMaxTokens() {
        return myState.maxTokens;
    }

    public float getTemperature() {
        return myState.temperature;
    }

    public int getRequestTimeout() {
        return myState.requestTimeout;
    }

    public void setOllamaHost(String ollamaHost) {
        myState.ollamaHost = ollamaHost;
    }

    public void setOllamaPort(int ollamaPort) {
        myState.ollamaPort = ollamaPort;
    }

    public void setOllamaModel(String ollamaModel) {
        myState.ollamaModel = ollamaModel;
    }
    
    public void setMaxTokens(int maxTokens) {   
        myState.maxTokens = maxTokens;
    }

    public void setTemperature(float temperature) {
        myState.temperature = temperature;
    }

    public void setRequestTimeout(int requestTimeout) {
        myState.requestTimeout = requestTimeout;
    }

    public void setCopyAsMarkdown(boolean copyAsMarkdown) {
        myState.copyAsMarkdown = copyAsMarkdown;
        notifyObservers("CONFIG_CHANGE:copyAsMarkdown", copyAsMarkdown);
    }




    @Nullable
    @Override
    public State getState() {
        return myState;
    }   

    @Override
    public void loadState(@NotNull State state) {
        LOG.info("Loading LLMService state: " + state);
        myState = state;
    }

    public void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutationList) {
        LOG.info("Generating unittest request for " + testCodeFile + " and " + sourceCodeFile);
        notifyObservers("START_LOADING", null);
        OllamaClient ollamaClient = new OllamaClient(
            myState.ollamaHost, 
            myState.ollamaModel, 
            myState.maxTokens, 
            myState.temperature, 
            myState.ollamaPort, 
            myState.requestTimeout
        );
        llmChatMediator.generateUnittestRequest(ollamaClient, testCodeFile, sourceCodeFile, mutationList);
    }

    // the mediator is dedicated to talk to the LLM, and when the response is ready, the service will propagate the response to the observers
    // so that on the UI layer, this is the place to notify all the observers
    // the mediator is not responsible for the UI, so it does not know the UI is a Swing UI
    @Override
    public void updateChatResponse(String chatResponse) {
        LOG.info("Received chat response: " + chatResponse);
        notifyObservers("STOP_LOADING", null);
        notifyObservers("CHAT_RESPONSE", chatResponse);
    }

}