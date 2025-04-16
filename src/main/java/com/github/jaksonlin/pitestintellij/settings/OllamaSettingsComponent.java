package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
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
        // Create main panel with a border layout to ensure full width usage
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create content panel with GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0; // Make components expand horizontally

        // Connection Settings Section
        JLabel connectionLabel = new JBLabel("<html><body><b>Connection Settings</b></body></html>");
        contentPanel.add(connectionLabel, c);

        // Host field
        JPanel hostPanel = new JPanel(new BorderLayout());
        hostPanel.add(new JBLabel("Host:"), BorderLayout.WEST);
        hostField.setToolTipText("The hostname or IP address of your Ollama server");
        hostPanel.add(hostField, BorderLayout.CENTER);
        contentPanel.add(hostPanel, c);

        // Port field
        JPanel portPanel = new JPanel(new BorderLayout());
        portPanel.add(new JBLabel("Port:"), BorderLayout.WEST);
        portField.setToolTipText("The port number of your Ollama server");
        portPanel.add(portField, BorderLayout.CENTER);
        contentPanel.add(portPanel, c);

        // Model Settings Section
        c.insets = new Insets(15, 0, 5, 0);
        JLabel modelLabel = new JBLabel("<html><body><b>Model Settings</b></body></html>");
        contentPanel.add(modelLabel, c);

        // Model field
        c.insets = new Insets(5, 0, 5, 0);
        JPanel modelPanel = new JPanel(new BorderLayout());
        modelPanel.add(new JBLabel("Model:"), BorderLayout.WEST);
        modelField.setToolTipText("The name of the Ollama model to use");
        modelPanel.add(modelField, BorderLayout.CENTER);
        contentPanel.add(modelPanel, c);

        // Max Tokens field
        JPanel maxTokensPanel = new JPanel(new BorderLayout());
        maxTokensPanel.add(new JBLabel("Max Tokens:"), BorderLayout.WEST);
        maxTokensField.setToolTipText("Maximum number of tokens in the response");
        maxTokensPanel.add(maxTokensField, BorderLayout.CENTER);
        contentPanel.add(maxTokensPanel, c);

        // Temperature field
        JPanel temperaturePanel = new JPanel(new BorderLayout());
        temperaturePanel.add(new JBLabel("Temperature:"), BorderLayout.WEST);
        temperatureField.setToolTipText("Controls randomness in the response (0.0 to 1.0)");
        temperaturePanel.add(temperatureField, BorderLayout.CENTER);
        contentPanel.add(temperaturePanel, c);

        // Timeout field
        JPanel timeoutPanel = new JPanel(new BorderLayout());
        timeoutPanel.add(new JBLabel("Timeout (ms):"), BorderLayout.WEST);
        timeoutField.setToolTipText("Request timeout in milliseconds");
        timeoutPanel.add(timeoutField, BorderLayout.CENTER);
        contentPanel.add(timeoutPanel, c);

        // Test Connection button
        c.insets = new Insets(15, 0, 5, 0);
        testConnectionButton = new JButton("Test Connection");
        contentPanel.add(testConnectionButton, c);

        // Add help text
        c.insets = new Insets(10, 0, 5, 0);
        JLabel helpText = new JBLabel("<html><body style='width: 100%'>" +
                "<p><b>Connection Help:</b></p>" +
                "<ul>" +
                "<li>Make sure Ollama is running on your system</li>" +
                "<li>Default host is localhost (127.0.0.1)</li>" +
                "<li>Default port is 11434</li>" +
                "</ul>" +
                "</body></html>");
        contentPanel.add(helpText, c);

        // Add action listener to test connection button
        testConnectionButton.addActionListener(e -> testConnection());

        // Add content panel to main panel
        mainPanel.add(contentPanel, BorderLayout.CENTER);
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