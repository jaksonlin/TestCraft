package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;

import com.github.jaksonlin.pitestintellij.llm.OllamaClient;
import javax.swing.*;
import java.awt.*;

public class OllamaSettingsComponent {
    private final JPanel mainPanel;
    private final JBTextField hostField = new JBTextField();
    private final JBTextField portField = new JBTextField();
    private final JBTextField modelField = new JBTextField();
    private final JBTextField maxTokensField = new JBTextField();
    private final JBTextField temperatureField = new JBTextField();
    private final JBTextField timeoutField = new JBTextField();
    private final JButton testConnectionButton;

    public OllamaSettingsComponent() {
        // Create the main panel with some padding
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Connection Settings Section
        addSectionHeader("Connection Settings", 0);

        // Host field
        c.gridx = 0;
        c.gridy = 1;
        mainPanel.add(new JBLabel("Host:"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        hostField.setToolTipText("The hostname or IP address of your Ollama server");
        mainPanel.add(hostField, c);

        // Port field
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        mainPanel.add(new JBLabel("Port:"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        portField.setToolTipText("The port number of your Ollama server");
        mainPanel.add(portField, c);

        // Model Settings Section
        addSectionHeader("Model Settings", 3);

        // Model field
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0.0;
        mainPanel.add(new JBLabel("Model:"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        modelField.setToolTipText("The name of the Ollama model to use");
        mainPanel.add(modelField, c);

        // Max Tokens field
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0.0;
        mainPanel.add(new JBLabel("Max Tokens:"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        maxTokensField.setToolTipText("Maximum number of tokens in the response");
        mainPanel.add(maxTokensField, c);

        // Temperature field
        c.gridx = 0;
        c.gridy = 6;
        c.weightx = 0.0;
        mainPanel.add(new JBLabel("Temperature:"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        temperatureField.setToolTipText("Controls randomness in the response (0.0 to 1.0)");
        mainPanel.add(temperatureField, c);

        // Timeout field
        c.gridx = 0;
        c.gridy = 7;
        c.weightx = 0.0;
        mainPanel.add(new JBLabel("Timeout (ms):"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        timeoutField.setToolTipText("Request timeout in milliseconds");
        mainPanel.add(timeoutField, c);

        // Test Connection button
        testConnectionButton = new JButton("Test Connection");
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        mainPanel.add(testConnectionButton, c);

        // Add action listener to test connection button
        testConnectionButton.addActionListener(e -> testConnection());
    }

    private void addSectionHeader(String text, int gridy) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridy;
        c.gridwidth = 2;
        c.insets = new Insets(10, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        
        JLabel header = new JBLabel(text);
        Font font = header.getFont();
        header.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
        mainPanel.add(header, c);
    }

    private void testConnection() {
        // Get the current settings
        String host = hostField.getText();
        String port = portField.getText();
        
        try {
            OllamaClient client = new OllamaClient(host, Integer.parseInt(port), Integer.parseInt(timeoutField.getText()));
            boolean success = client.testConnection();
            if (success) {
                JOptionPane.showMessageDialog(mainPanel,
                    "Successfully connected to Ollama server!",
                    "Connection Test",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel,
                    "Failed to connect to Ollama server",
                    "Connection Test",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel,
                "Error testing connection: " + e.getMessage(),
                "Connection Test",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return hostField;
    }

    @NotNull
    public String getHostText() {
        return hostField.getText();
    }

    public void setHostText(@NotNull String text) {
        hostField.setText(text);
    }

    @NotNull
    public String getPortText() {
        return portField.getText();
    }

    public void setPortText(@NotNull String text) {
        portField.setText(text);
    }

    @NotNull
    public String getModelText() {
        return modelField.getText();
    }

    public void setModelText(@NotNull String text) {
        modelField.setText(text);
    }

    @NotNull
    public String getMaxTokensText() {
        return maxTokensField.getText();
    }

    public void setMaxTokensText(@NotNull String text) {
        maxTokensField.setText(text);
    }

    @NotNull
    public String getTemperatureText() {
        return temperatureField.getText();
    }

    public void setTemperatureText(@NotNull String text) {
        temperatureField.setText(text);
    }

    @NotNull
    public String getTimeoutText() {
        return timeoutField.getText();
    }

    public void setTimeoutText(@NotNull String text) {
        timeoutField.setText(text);
    }
} 