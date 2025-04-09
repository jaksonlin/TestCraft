package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.llm.OllamaClient;
import com.github.jaksonlin.pitestintellij.observers.ObserverBase;
import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class LLMService extends ObserverBase {
    private static final Logger LOG = Logger.getInstance(LLMService.class);




    public void notifyLLMResponse(String suggestions) {
        super.notifyObservers(suggestions);
    }
}