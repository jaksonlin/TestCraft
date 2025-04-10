package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.github.jaksonlin.pitestintellij.util.OllamaClient;

import java.util.List;

public interface ILLMChatMediator {
    void generateUnittestRequest(OllamaClient ollamaClient, String testCodeFile, String sourceCodeFile, List<Mutation> mutationList);
    void register(ILLMChatClient chatClient);
    String dryRunGetPrompt(String testCodeFile, String sourceCodeFile, List<Mutation> mutations);
}
