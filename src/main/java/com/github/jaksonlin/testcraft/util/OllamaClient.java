package com.github.jaksonlin.testcraft.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OllamaClient {
    private static final Logger LOG = Logger.getInstance(OllamaClient.class);
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final int timeoutSeconds;
    private final String model;
    private final int maxTokens;
    private final float temperature;

    public OllamaClient(String host, String model, int maxTokens, float temperature, int port, int timeoutSeconds) {
        this.baseUrl = String.format("http://%s:%d", host, port);
        this.objectMapper = new ObjectMapper();
        this.timeoutSeconds = timeoutSeconds;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeoutSeconds * 1000)
                .setConnectionRequestTimeout(timeoutSeconds * 1000)
                .setSocketTimeout(timeoutSeconds * 1000)
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    public boolean testConnection() {
        try {
            HttpGet request = new HttpGet(baseUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return response.getStatusLine().getStatusCode() == 200;
            }
        } catch (Exception e) {
            LOG.warn("Failed to test connection to Ollama server", e);
            return false;
        }
    }

    public String chatCompletion(List<Message> messages) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("temperature", temperature);
        requestBody.put("num_predict", maxTokens);

        HttpPost request = new HttpPost(baseUrl + "/api/chat");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), "UTF-8"));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                String errorBody = EntityUtils.toString(response.getEntity());
                LOG.error("Error from Ollama API: " + errorBody);
                throw new IOException("Failed to get response from Ollama API: " + response.getStatusLine().getStatusCode());
            }

            String responseBody = EntityUtils.toString(response.getEntity());
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            return ((Map<String, String>) responseMap.get("message")).get("content");
        }
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