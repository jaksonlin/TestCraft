package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.mediators.ILLMChatMediator;
import com.github.jaksonlin.pitestintellij.mediators.LLMChatMediatorImpl;
import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
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
    storages = @Storage(value = "$APP_CONFIG$/LLMService.xml")
)
public final class LLMService
        extends ObserverBase
        implements ILLMChatClient, BasicEventObserver, PersistentStateComponent<LLMService.State> {

    private static final Logger LOG = Logger.getInstance(LLMService.class);
    private final ILLMChatMediator llmChatMediator = new LLMChatMediatorImpl();


    public LLMService() {
        llmChatMediator.register(this);
    }

    public static class State {
        public String ollamaHost = "localhost";
        public int ollamaPort = 11434;
        public String ollamaModel = "deepseek-r1:32b";
        public int maxTokens = 2000;
        public float temperature = 0.7f;
        public int requestTimeout = 60;  // seconds
        public boolean copyAsMarkdown = false;

        public State() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return ollamaHost.equals(state.ollamaHost) &&
                    ollamaPort == state.ollamaPort &&
                    ollamaModel.equals(state.ollamaModel) &&
                    maxTokens == state.maxTokens &&
                    Float.compare(state.temperature, temperature) == 0 &&
                    requestTimeout == state.requestTimeout &&
                    copyAsMarkdown == state.copyAsMarkdown;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ollamaHost, ollamaPort, ollamaModel, maxTokens, temperature, requestTimeout, copyAsMarkdown);
        }
    }

    private State myState = new State();

    @Nullable
    @Override
    public State getState() {
        return myState;
    }   

    @Override
    public void loadState(@NotNull State state) {
        LOG.info("Loading LLMService state: " + state);
        myState = state;
        notifyObservers("CONFIG_CHANGE:copyAsMarkdown", state.copyAsMarkdown);
    }

    public void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutationList) {
        LOG.info("Generating unittest request for " + testCodeFile + " and " + sourceCodeFile);

        llmChatMediator.setOllamaClient(new OllamaClient(
            myState.ollamaHost, 
            myState.ollamaModel, 
            myState.maxTokens, 
            myState.temperature, 
            myState.ollamaPort, 
            myState.requestTimeout
        ));
        llmChatMediator.generateUnittestRequest(testCodeFile, sourceCodeFile, mutationList);
    }

    // the mediator is dedicated to talk to the LLM, and when the response is ready, the service will propagate the response to the observers
    // so that on the UI layer, this is the place to notify all the observers
    // the mediator is not responsible for the UI, so it does not know the UI is a Swing UI
    @Override
    public void updateChatResponse(String responseType, String chatResponse) {
        LOG.info("Received chat response: " + chatResponse);
        notifyObservers("CHAT_RESPONSE:" + responseType, chatResponse);
    }

    public void handleChatMessage(String message) {
        LOG.info("Received chat message: " + message);
        llmChatMediator.setOllamaClient(new OllamaClient(
            myState.ollamaHost, 
            myState.ollamaModel, 
            myState.maxTokens, 
            myState.temperature, 
            myState.ollamaPort, 
            myState.requestTimeout
        ));
        llmChatMediator.handleChatMessage(message);
    }

    public String dryRunGetPrompt(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {
        return llmChatMediator.dryRunGetPrompt(testCodeFile, sourceCodeFile, mutations);
    }

    public void onEventHappen(String eventName, Object eventObj) {
        // Handle events from LLMChatMediator if needed
        switch (eventName) {
            case "CLEAR_CHAT":
                this.llmChatMediator.clearChat();
                break;
            default:
                break;
        }
    }

    public String getChatHistory() {
        return llmChatMediator.getChatHistory();
    }

    public void propagateConfigChange() {
        notifyObservers("CONFIG_CHANGE:copyAsMarkdown", myState.copyAsMarkdown);
    }
}