package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.util.Mutation;

import java.util.List;

public interface ILLMChatMediator {
    void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutationList);
    void register(ILLMChatClient clientUI);
}
