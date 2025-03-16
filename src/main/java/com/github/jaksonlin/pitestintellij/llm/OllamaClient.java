package com.github.jaksonlin.pitestintellij.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OllamaClient {
    private static final Logger LOG = Logger.getInstance(OllamaClient.class);
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaClient(String host, int port) {
        this.baseUrl = String.format("http://%s:%d", host, port);
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public String chatCompletion(String model, List<Message> messages) throws IOException, InterruptedException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
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