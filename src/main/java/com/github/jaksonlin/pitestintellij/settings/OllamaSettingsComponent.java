package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;

import com.github.jaksonlin.pitestintellij.llm.OllamaClient;
import javax.swing.*;
import java.awt.*;

public class OllamaSettingsComponent {
    private final JPanel mainPanel;
    private final JBTextField hostField = new JBTextField();
    private final JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(11434, 1, 65535, 1));
    private final JBTextField modelField = new JBTextField();
    private final JSpinner maxTokensSpinner = new JSpinner(new SpinnerNumberModel(2000, 100, 10000, 100));
    private final JSpinner temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.7, 0.0, 2.0, 0.1));
    private final JSpinner timeoutSpinner = new JSpinner(new SpinnerNumberModel(60, 10, 300, 10));
    private final JButton testConnectionButton = new JButton("Test Connection");

    public OllamaSettingsComponent() {
        // Configure spinners to allow decimal input for temperature
        JSpinner.NumberEditor temperatureEditor = new JSpinner.NumberEditor(temperatureSpinner, "0.0");
        temperatureSpinner.setEditor(temperatureEditor);

        // Add test connection action
        testConnectionButton.addActionListener(e -> testConnection());

        // Create help texts
        String modelHelp = "Model to use for code analysis (e.g., deepseek-r1:32b, codellama:13b)";
        String tempHelp = "Controls randomness in responses (0.0 = deterministic, 2.0 = very random)";
        String tokenHelp = "Maximum number of tokens in the response";
        String timeoutHelp = "Request timeout in seconds";

        // Build the form
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Ollama Host: "), hostField, true)
                .addLabeledComponent(new JBLabel("Port: "), portSpinner, true)
                .addLabeledComponent(new JBLabel("Model: "), modelField, true)
                .addTooltip(modelHelp)
                .addLabeledComponent(new JBLabel("Max Tokens: "), maxTokensSpinner, true)
                .addTooltip(tokenHelp)
                .addLabeledComponent(new JBLabel("Temperature: "), temperatureSpinner, true)
                .addTooltip(tempHelp)
                .addLabeledComponent(new JBLabel("Timeout (seconds): "), timeoutSpinner, true)
                .addTooltip(timeoutHelp)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // Add test connection button in a separate panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(testConnectionButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void testConnection() {
        try {
            OllamaClient testClient = new OllamaClient(
                    getOllamaHost(),
                    getOllamaPort(),
                    getRequestTimeout()
            );
            boolean success = testClient.testConnection();
            if (success) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "Successfully connected to Ollama server!",
                        "Connection Test",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "Could not connect to Ollama server. Please check your settings.",
                        "Connection Test Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Error testing connection: " + e.getMessage(),
                    "Connection Test Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public String getOllamaHost() {
        return hostField.getText();
    }

    public void setOllamaHost(String host) {
        hostField.setText(host);
    }

    public int getOllamaPort() {
        return (Integer) portSpinner.getValue();
    }

    public void setOllamaPort(int port) {
        portSpinner.setValue(port);
    }

    public String getOllamaModel() {
        return modelField.getText();
    }

    public void setOllamaModel(String model) {
        modelField.setText(model);
    }

    public int getMaxTokens() {
        return (Integer) maxTokensSpinner.getValue();
    }

    public void setMaxTokens(int tokens) {
        maxTokensSpinner.setValue(tokens);
    }

    public float getTemperature() {
        return ((Number) temperatureSpinner.getValue()).floatValue();
    }

    public void setTemperature(float temperature) {
        temperatureSpinner.setValue(temperature);
    }

    public int getRequestTimeout() {
        return (Integer) timeoutSpinner.getValue();
    }

    public void setRequestTimeout(int timeout) {
        timeoutSpinner.setValue(timeout);
    }
} 