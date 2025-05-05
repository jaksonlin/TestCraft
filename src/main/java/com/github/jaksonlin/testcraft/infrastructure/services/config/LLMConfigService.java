package com.github.jaksonlin.testcraft.infrastructure.services.config;

import com.github.jaksonlin.testcraft.infrastructure.messaging.mediators.ILLMChatMediator;
import com.github.jaksonlin.testcraft.infrastructure.messaging.mediators.LLMChatMediatorImpl;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.github.jaksonlin.testcraft.util.Mutation;
import com.github.jaksonlin.testcraft.util.OllamaClient;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.BasicEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ChatEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.LLMConfigEvent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
@Service(Service.Level.APP)
@State(
    name = "com.github.jaksonlin.testcraft.infrastructure.services.LLMService",
    storages = @Storage(value = "$APP_CONFIG$/LLMService.xml")
)
public final class LLMConfigService
        extends BasicEventObserver
        implements PersistentStateComponent<LLMConfigService.State> {

    private static final Logger LOG = Logger.getInstance(LLMConfigService.class);
    private final ILLMChatMediator llmChatMediator = new LLMChatMediatorImpl();

    public static LLMConfigService getInstance() {
        return ApplicationManager.getApplication().getService(LLMConfigService.class);
    }

    public LLMConfigService() {
        EventBusService.getInstance().register(this);
    }

    public static class State {
        public String ollamaHost = "localhost";
        public int ollamaPort = 11434;
        public String ollamaModel = "deepseek-r1:32b";
        public int maxTokens = 2000;
        public float temperature = 0.7f;
        public int requestTimeout = 60;  // seconds
        public boolean copyAsMarkdown = true;

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
        EventBusService.getInstance().post(new LLMConfigEvent(LLMConfigEvent.CONFIG_CHANGE_COPY_AS_MARKDOWN, state.copyAsMarkdown));
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

    public String dryRunGetPrompt(String testClassName, String sourceClassName, List<Mutation> mutations) {
        return llmChatMediator.dryRunGetPrompt(testClassName, sourceClassName, mutations);
    }

    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        // Handle events from LLMChatMediator if needed
        switch (eventName) {
            case ChatEvent.CHAT_REQUEST:
                handleChatMessage(eventObj.toString());
                break;
            case ChatEvent.CLEAR_CHAT:
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
        EventBusService.getInstance().post(new LLMConfigEvent(LLMConfigEvent.CONFIG_CHANGE_COPY_AS_MARKDOWN, myState.copyAsMarkdown));
    }
}