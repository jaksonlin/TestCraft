package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.MyBundle;
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
    private OllamaClient ollamaClient;
    private final List<OllamaClient.Message> messageHistory = new ArrayList<>();

    public void setOllamaClient(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public void clearChat() {
        messageHistory.clear();
    }

    public String getChatHistory() {
        return messageHistory.stream()
                .map(message -> message.getRole() + ": " + message.getContent())
                .collect(Collectors.joining("\n"));
    }

    // generate unittest using mutation result, this will clear the chat history
    @Override
    public void generateUnittestRequest(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {
        executorService.submit(() -> {
            try {
                // Test connection before attempting to send message
                if (!ollamaClient.testConnection()) {
                    if (chatClient != null) {
                        SwingUtilities.invokeLater(() -> chatClient.updateChatResponse("ERROR", 
                            MyBundle.message("llm.error.connection")));
                    }
                    return;
                }
                List<OllamaClient.Message> messages = createPromptOnly(testCodeFile, sourceCodeFile, mutations);
                messageHistory.addAll(messages);
                String rawResponse = ollamaClient.chatCompletion(messageHistory);
                messageHistory.add(new OllamaClient.Message("assistant", rawResponse));
                String formattedResponse = formatResponse(rawResponse);
                SwingUtilities.invokeLater(() -> chatClient.updateChatResponse("UNIT_TEST_REQUEST", formattedResponse));
                
            } catch (Exception e) {
                LOG.error("Failed to generate unit test suggestions", e);
                if (chatClient != null) {
                    SwingUtilities.invokeLater(() -> chatClient.updateChatResponse("ERROR", "Error: " + e.toString()));
                }
            }
        });
    }

    // normal chat
    @Override
    public void handleChatMessage(String message) {
        executorService.submit(() -> {
            try {
                // Test connection before attempting to send message
                if (!ollamaClient.testConnection()) {
                    if (chatClient != null) {
                        SwingUtilities.invokeLater(() -> chatClient.updateChatResponse("ERROR", 
                            MyBundle.message("llm.error.connection")));
                    }
                    return;
                }
                messageHistory.add(new OllamaClient.Message("user", message));
                String rawResponse = ollamaClient.chatCompletion(messageHistory);
                messageHistory.add(new OllamaClient.Message("assistant", rawResponse));
                // Format the response
                String formattedResponse = formatResponse(rawResponse);
                chatClient.updateChatResponse("CHAT_MESSAGE", formattedResponse);
            } catch (Exception e) {
                LOG.error("Failed to respond to chat message", e);
                if (chatClient != null) {
                    SwingUtilities.invokeLater(() -> chatClient.updateChatResponse("ERROR", "Error: " + e.toString()));
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
        List<OllamaClient.Message> promptOnlyMessages = new ArrayList<>();
        // Read source files
        String sourceCode = Files.readString(Paths.get(sourceCodeFile));
        String testCode = Files.readString(Paths.get(testCodeFile));
        
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

        

        // System message to set context
        promptOnlyMessages.add(new OllamaClient.Message("system", MyBundle.message("llm.prompt.system")));

        // User message with the structured data
        String prompt = String.format(MyBundle.message("llm.prompt.user"),
                sourceCode,
                testCode,
                totalMutations,
                killedMutations,
                (killedMutations * 100.0 / totalMutations),
                survivedMutations,
                (survivedMutations * 100.0 / totalMutations),
                analysisBuilder.toString()
        );
        promptOnlyMessages.add(new OllamaClient.Message("user", prompt));
        return promptOnlyMessages;
    }

    @Override
    public String dryRunGetPrompt(String testCodeFile, String sourceCodeFile, List<Mutation> mutations) {
        try {   
            List<OllamaClient.Message> dryRunMessages = createPromptOnly(testCodeFile, sourceCodeFile, mutations);
            
            StringBuilder markdownBuilder = new StringBuilder();
            for (OllamaClient.Message message : dryRunMessages) {
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

 



    @Override
    public void register(ILLMChatClient chatClient) {
        this.chatClient = chatClient;
    }
}
