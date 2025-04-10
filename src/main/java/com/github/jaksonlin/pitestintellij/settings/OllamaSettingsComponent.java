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
    private final JCheckBox copyAsMarkdownCheckbox;

    public OllamaSettingsComponent() {
        // Create main panel with a border layout to ensure full width usage
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create content panel with GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        // Connection Settings Section
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        JLabel connectionLabel = new JBLabel("<html><body><b>Connection Settings</b></body></html>");
        contentPanel.add(connectionLabel, c);

        // Host field
        addLabelAndField(contentPanel, "Host:", hostField, 1,
                "The hostname or IP address of your Ollama server");

        // Port field
        addLabelAndField(contentPanel, "Port:", portField, 2,
                "The port number of your Ollama server");

        // Model Settings Section
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(15, 5, 5, 5);
        JLabel modelLabel = new JBLabel("<html><body><b>Model Settings</b></body></html>");
        contentPanel.add(modelLabel, c);

        // Model field
        addLabelAndField(contentPanel, "Model:", modelField, 4,
                "The name of the Ollama model to use");

        // Max Tokens field
        addLabelAndField(contentPanel, "Max Tokens:", maxTokensField, 5,
                "Maximum number of tokens in the response");

        // Temperature field
        addLabelAndField(contentPanel, "Temperature:", temperatureField, 6,
                "Controls randomness in the response (0.0 to 1.0)");

        // Timeout field
        addLabelAndField(contentPanel, "Timeout (ms):", timeoutField, 7,
                "Request timeout in milliseconds");

        // Output Settings Section
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 8;
        c.insets = new Insets(15, 5, 5, 5);
        JLabel outputLabel = new JBLabel("<html><body><b>Output Settings</b></body></html>");
        contentPanel.add(outputLabel, c);

        // Copy as Markdown checkbox
        c.gridy = 9;
        c.insets = new Insets(5, 5, 5, 5);
        copyAsMarkdownCheckbox = new JCheckBox("Copy output as Markdown");
        copyAsMarkdownCheckbox.setToolTipText("When enabled, copied output will be in Markdown format. When disabled, copies the rendered output.");
        contentPanel.add(copyAsMarkdownCheckbox, c);

        // Test Connection button
        c.gridy = 10;
        c.insets = new Insets(15, 5, 5, 5);
        testConnectionButton = new JButton("Test Connection");
        contentPanel.add(testConnectionButton, c);

        // Add help text
        c.gridy = 11;
        c.insets = new Insets(15, 5, 5, 5);
        JLabel helpText = new JBLabel("<html><body style='width: 300px'>" +
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

    private void addLabelAndField(JPanel panel, String labelText, JComponent field, int gridy, String tooltip) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        // Label
        c.gridx = 0;
        c.gridy = gridy;
        c.gridwidth = 1;
        c.weightx = 0.0;
        JLabel label = new JBLabel(labelText);
        label.setPreferredSize(new Dimension(100, label.getPreferredSize().height));
        panel.add(label, c);

        // Field
        c.gridx = 1;
        c.weightx = 1.0;
        field.setToolTipText(tooltip);
        panel.add(field, c);
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

    public boolean getCopyAsMarkdown() {
        return copyAsMarkdownCheckbox.isSelected();
    }

    public void setCopyAsMarkdown(boolean selected) {
        copyAsMarkdownCheckbox.setSelected(selected);
    }
} 