package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.github.jaksonlin.pitestintellij.util.OllamaClient;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LLMChatMediatorImpl implements ILLMChatMediator {
    private static final Logger LOG = Logger.getInstance(LLMChatMediatorImpl.class);
    private ILLMChatClient chatClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @Override
    public void generateUnittestRequest(OllamaClient ollamaClient, String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {
        executorService.submit(() -> {
            try {
                String rawResponse = LLmChatRequest(ollamaClient, testCodeFile, sourceCodeFile, mutations);
                String formattedResponse = formatResponse(rawResponse);
                SwingUtilities.invokeLater(() -> chatClient.updateChatResponse(formattedResponse));
                
            } catch (Exception e) {
                LOG.error("Failed to generate unit test suggestions", e);
                if (chatClient != null) {
                    SwingUtilities.invokeLater(() -> chatClient.updateChatResponse("Error: " + e.getMessage()));
                }
            }
        });
    }

    private String formatResponse(String rawResponse) {
        // Process code blocks
        rawResponse = processCodeBlocks(rawResponse);
        
        // Process headers
        rawResponse = processHeaders(rawResponse);
        
        // Process lists
        rawResponse = processLists(rawResponse);
        
        return rawResponse;
    }

    private String processCodeBlocks(String text) {
        Pattern codeBlockPattern = Pattern.compile("```(\\w*)\\s*\\n([\\s\\S]*?)```");
        Matcher matcher = codeBlockPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String language = matcher.group(1);
            String code = matcher.group(2);
            if (!code.endsWith("\n")) {
                code += "\n";
            }
            matcher.appendReplacement(sb, "```" + language + "\n" + code + "```\n");
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    private String processHeaders(String text) {
        text = text.replaceAll("^(#{1,6})([^\\s#])", "$1 $2");
        text = text.replaceAll("<h1>([^<]+)</h1>", "# $1");
        text = text.replaceAll("<h2>([^<]+)</h2>", "## $1");
        text = text.replaceAll("<h3>([^<]+)</h3>", "### $1");
        return text;
    }

    private String processLists(String text) {
        text = text.replaceAll("^•\\s", "* ");
        text = text.replaceAll("^-\\s", "* ");
        text = text.replaceAll("^\\d+\\.\\s", "1. ");
        return text;
    }

    private List<OllamaClient.Message> createPromptOnly(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) throws IOException {
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
        return messages;
    }

    @Override
    public String dryRunGetPrompt(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {
        try {   
            List<OllamaClient.Message> messages = createPromptOnly(testCodeFile, sourceCodeFile, mutations);
            
            StringBuilder markdownBuilder = new StringBuilder();
            for (OllamaClient.Message message : messages) {
                String role = message.getRole();
                String content = message.getContent();
                
                // Format based on role
                switch (role) {
                    case "system":
                        markdownBuilder.append("## System Message\n\n")
                                     .append(content)
                                     .append("\n\n");
                        break;
                    case "user":
                        markdownBuilder.append("## User Request\n\n")
                                     .append(content)
                                     .append("\n\n");
                        break;
                    case "assistant":
                        markdownBuilder.append("## Assistant Response\n\n")
                                     .append(content)
                                     .append("\n\n");
                        break;
                    default:
                        markdownBuilder.append("## ").append(role).append("\n\n")
                                     .append(content)
                                     .append("\n\n");
                }
            }
            
            return markdownBuilder.toString();
        } catch (IOException e) {
            LOG.error("Failed to generate unit test suggestions", e);
            return "Error: " + e.getMessage();
        }
    }

    private String LLmChatRequest(OllamaClient ollamaClient, String testCodeFile, String sourceCodeFile, List<Mutation> mutations) throws IOException {
        
        try {
            List<OllamaClient.Message> messages = createPromptOnly(testCodeFile, sourceCodeFile, mutations);
            return ollamaClient.chatCompletion(messages);
        } catch (Exception e) {
            throw new IOException("Failed to generate unit test suggestions: " + e.getMessage());
        }
    }

    @Override
    public void register(ILLMChatClient chatClient) {
        this.chatClient = chatClient;
    }
}
