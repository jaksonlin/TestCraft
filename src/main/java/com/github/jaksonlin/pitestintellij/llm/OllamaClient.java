package com.github.jaksonlin.pitestintellij.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jaksonlin.pitestintellij.settings.OllamaSettingsState;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OllamaClient {
    private static final Logger LOG = Logger.getInstance(OllamaClient.class);
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final int timeoutSeconds;

    public OllamaClient(String host, int port, int timeoutSeconds) {
        this.baseUrl = String.format("http://%s:%d", host, port);
        this.objectMapper = new ObjectMapper();
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public OllamaClient() {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        this.baseUrl = String.format("http://%s:%d", settings.ollamaHost, settings.ollamaPort);
        this.objectMapper = new ObjectMapper();
        this.timeoutSeconds = settings.requestTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(settings.requestTimeout))
                .build();
    }

    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            LOG.warn("Failed to test connection to Ollama server", e);
            return false;
        }
    }

    public String chatCompletion(List<Message> messages) throws IOException, InterruptedException {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", settings.ollamaModel);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", settings.temperature);
        requestBody.put("num_predict", settings.maxTokens);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOG.error("Error from Ollama API: " + response.body());
            throw new IOException("Failed to get response from Ollama API: " + response.statusCode());
        }

        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        return ((Map<String, String>) responseMap.get("message")).get("content");
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
} 