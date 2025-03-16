package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.llm.OllamaClient;
import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.github.jaksonlin.pitestintellij.util.Pair;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LLMChatMediatorImpl implements ILLMChatMediator {
    private static final Logger LOG = Logger.getInstance(LLMChatMediatorImpl.class);
    private final OllamaClient ollamaClient;
    private static final String DEFAULT_MODEL = "deepseek-r1:32b";
    private ILLMChatUI clientUI;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LLMChatMediatorImpl() {
        this.ollamaClient = new OllamaClient("localhost", 11434);
    }

    @Override
    public void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {

        executorService.submit(() -> {
            String chatResponse = LLmChatRequest(testCodeFile, sourceCodeFile, mutations);
            if (clientUI != null) {
                SwingUtilities.invokeLater(() -> clientUI.updateChatResponse(chatResponse));
            }
        });
    }

    private String LLmChatRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {
        try {
            // Read source files
            String sourceCode = Files.readString(Paths.get(testCodeFile));
            String testCode = Files.readString(Paths.get(sourceCodeFile));
            // Group mutations by line number
            Map<Integer, List<Mutation>> mutationsByLine = mutations.stream()
                    .collect(Collectors.groupingBy(Mutation::getLineNumber));

            // Build the detailed analysis string
            StringBuilder analysisBuilder = new StringBuilder();
            mutationsByLine.forEach((line, lineMutations) -> {
                analysisBuilder.append(String.format("\nLine %d:\n", line));
                lineMutations.forEach(mutation -> {
                    analysisBuilder.append(String.format("- Mutation: %s\n  Status: %s\n  Description: %s\n",
                            mutation.getMutator(),
                            mutation.getStatus(),
                            mutation.getDescription()));
                });
            });

            // Calculate statistics
            long totalMutations = mutations.size();
            long killedMutations = mutations.stream()
                    .filter(m -> "KILLED".equals(m.getStatus()))
                    .count();
            long survivedMutations = mutations.stream()
                    .filter(m -> "SURVIVED".equals(m.getStatus()))
                    .count();

            List<OllamaClient.Message> messages = new ArrayList<>();

            // System message to set context
            messages.add(new OllamaClient.Message("system",
                    "You are a specialized code analysis assistant focused on improving unit test coverage based on mutation testing results. " +
                            "Your task is to first analysis the mutation result, look at the lines that have both `KILLED` and `SURVIVED` mutations; " +
                            "and then look at the unit tests that can execute the mutations, exam how the test `KILLED` the mutation and why some `SURVIVED`. " +
                            "Finally, suggest specific unit tests to handle the `SURVIVED` mutations. "
            ));

            // User message with the structured data
            String prompt = String.format(
                    "Please analyze the following mutation testing results and suggest specific unit tests:\n\n" +
                            "=== Source Code Under Test ===\n" +
                            "%s\n\n" +
                            "=== Current Test File ===\n" +
                            "%s\n\n" +
                            "=== Mutation Testing Statistics ===\n" +
                            "Total Mutations: %d\n" +
                            "Killed Mutations: %d (%.1f%%)\n" +
                            "Survived Mutations: %d (%.1f%%)\n\n" +
                            "=== Detailed Mutation Analysis ===\n" +
                            "%s\n\n" +
                            "Based on the above analysis, please provide:\n" +
                            "1. Specific test cases to handle survived mutations\n" +
                            "2. The exact assertions needed for each test case\n" +
                            "3. Brief explanations of why each test is necessary\n" +
                            "Format your response in markdown with code blocks for the test cases.",
                    sourceCode,
                    testCode,
                    totalMutations,
                    killedMutations,
                    (killedMutations * 100.0 / totalMutations),
                    survivedMutations,
                    (survivedMutations * 100.0 / totalMutations),
                    analysisBuilder.toString()
            );

            messages.add(new OllamaClient.Message("user", prompt));

            try {
                return ollamaClient.chatCompletion(DEFAULT_MODEL, messages);
            } catch (Exception e) {
                return "Failed to generate unit test suggestions";
            }
        } catch (IOException e) {
            return "Failed to read source files";
        }
    }

    @Override
    public void register(ILLMChatUI clientUI) {
        this.clientUI = clientUI;
    }
}
