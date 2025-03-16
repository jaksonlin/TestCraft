package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.llm.OllamaClient;
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
public final class LLMService {
    private static final Logger LOG = Logger.getInstance(LLMService.class);
    private final OllamaClient ollamaClient;
    private static final String DEFAULT_MODEL = "deepseek-r1:32b";

    public LLMService() {
        this.ollamaClient = new OllamaClient("localhost", 11434);
    }

    public String generateUnitTestSuggestions(PitestContext context) {
        try {
            // Read source files
            String sourceCode = Files.readString(Paths.get(context.getTargetClassFilePath()));
            String testCode = Files.readString(Paths.get(context.getTestFilePath()));
            
            // Get mutation results
            List<Mutation> mutations = context.getMutationResults();
            
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
                LOG.error("Failed to generate unit test suggestions", e);
                return "Failed to generate suggestions: " + e.getMessage();
            }
        } catch (IOException e) {
            LOG.error("Failed to read source files", e);
            return "Failed to read source files: " + e.getMessage();
        }
    }
}