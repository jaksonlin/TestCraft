package com.github.jaksonlin.testcraft.infrastructure.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import com.github.jaksonlin.testcraft.util.OllamaClient;
import com.github.jaksonlin.testcraft.util.MyBundle;
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
    private final JCheckBox copyAsMarkdownCheckbox;

    public OllamaSettingsComponent() {
        // Create main panel with a border layout to ensure full width usage
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(JBUI.Borders.empty(10));

        // Create content panel with BoxLayout for vertical stacking
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Connection Settings Section
        JPanel connectionPanel = createSectionPanel(MyBundle.message("llm.settings.connection.title"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = JBUI.insets(5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        // Host field
        addLabelAndField(connectionPanel, MyBundle.message("llm.settings.host.label"), hostField,
                MyBundle.message("llm.settings.host.tooltip"));

        // Port field
        addLabelAndField(connectionPanel, MyBundle.message("llm.settings.port.label"), portField,
                MyBundle.message("llm.settings.port.tooltip"));

        contentPanel.add(connectionPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Model Settings Section
        JPanel modelPanel = createSectionPanel(MyBundle.message("llm.settings.model.title"));

        // Model field
        addLabelAndField(modelPanel, MyBundle.message("llm.settings.model.label"), modelField,
                MyBundle.message("llm.settings.model.tooltip"));

        // Max Tokens field
        addLabelAndField(modelPanel, MyBundle.message("llm.settings.maxTokens.label"), maxTokensField,
                MyBundle.message("llm.settings.maxTokens.tooltip"));

        // Temperature field
        addLabelAndField(modelPanel, MyBundle.message("llm.settings.temperature.label"), temperatureField,
                MyBundle.message("llm.settings.temperature.tooltip"));

        // Timeout field
        addLabelAndField(modelPanel, MyBundle.message("llm.settings.timeout.label"), timeoutField,
                MyBundle.message("llm.settings.timeout.tooltip"));

        contentPanel.add(modelPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Output Settings Section
        JPanel outputPanel = createSectionPanel(MyBundle.message("llm.settings.output.title"));
        copyAsMarkdownCheckbox = new JCheckBox(MyBundle.message("llm.settings.copyMarkdown.label"));
        copyAsMarkdownCheckbox.setToolTipText(MyBundle.message("llm.settings.copyMarkdown.tooltip"));
        copyAsMarkdownCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputPanel.add(copyAsMarkdownCheckbox);

        contentPanel.add(outputPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Test Connection Section
        JPanel testPanel = createSectionPanel(MyBundle.message("llm.settings.test.title"));
        JButton testConnectionButton = new JButton(MyBundle.message("llm.settings.test.button"));
        testConnectionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        testPanel.add(testConnectionButton);

        // Add help text
        JLabel helpText = new JBLabel("<html><body style='width: 300px'>" +
                "<p><b>" + MyBundle.message("llm.settings.help.title") + "</b></p>" +
                "<ul>" +
                "<li>" + MyBundle.message("llm.settings.help.running") + "</li>" +
                "<li>" + MyBundle.message("llm.settings.help.host") + "</li>" +
                "<li>" + MyBundle.message("llm.settings.help.port") + "</li>" +
                "</ul>" +
                "</body></html>");
        helpText.setAlignmentX(Component.LEFT_ALIGNMENT);
        helpText.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        testPanel.add(helpText);

        contentPanel.add(testPanel);

        // Add action listener to test connection button
        testConnectionButton.addActionListener(e -> testConnection());

        // Add content panel to main panel with scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            JBUI.Borders.customLine(UIManager.getColor("Component.borderColor"), 1),
            JBUI.Borders.empty(10)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JBLabel("<html><body><b>" + title + "</b></body></html>");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(JBUI.Borders.empty(0, 0, 5, 0));
        panel.add(titleLabel);

        return panel;
    }

    private void addLabelAndField(JPanel panel, String labelText, JComponent field, String tooltip) {
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JBLabel(labelText);
        label.setPreferredSize(new Dimension(100, label.getPreferredSize().height));
        fieldPanel.add(label, BorderLayout.WEST);
        
        field.setToolTipText(tooltip);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        panel.add(fieldPanel);
        panel.add(Box.createVerticalStrut(5));
    }

    private void testConnection() {
        // Get the current settings
        String host = hostField.getText();
        String port = portField.getText();
        
        try {
            OllamaClient client = new OllamaClient(host, "dummy", 100, 0.5f, Integer.parseInt(port), Integer.parseInt(timeoutField.getText()));
            boolean success = client.testConnection();
            if (success) {
                JOptionPane.showMessageDialog(mainPanel,
                    "Successfully connected to Ollama server!",
                    MyBundle.message("llm.settings.test.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel,
                    MyBundle.message("llm.error.connection"),
                    MyBundle.message("llm.settings.test.title"),
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel,
                MyBundle.message("llm.error.connection") + ": " + e.getMessage(),
                MyBundle.message("llm.settings.test.title"),
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