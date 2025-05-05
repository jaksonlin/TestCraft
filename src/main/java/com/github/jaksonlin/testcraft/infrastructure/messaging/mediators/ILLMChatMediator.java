package com.github.jaksonlin.testcraft.infrastructure.messaging.mediators;

import com.github.jaksonlin.testcraft.util.Mutation;
import com.github.jaksonlin.testcraft.util.OllamaClient;

import java.util.List;

public interface ILLMChatMediator {
    void setOllamaClient(OllamaClient ollamaClient);
    void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutationList);
    String dryRunGetPrompt(String testClassName, String sourceClassName, List<Mutation> mutations);
    void handleChatMessage(String message);
    void clearChat();
    String getChatHistory();
}
