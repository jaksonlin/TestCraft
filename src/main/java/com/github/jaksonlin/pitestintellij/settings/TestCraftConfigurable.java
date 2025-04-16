package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

import com.intellij.openapi.options.Configurable;

class TestCraftConfigurable implements SearchableConfigurable.Parent {
    
    @Override
    public @NotNull String getId() {
        return "com.github.jaksonlin.pitestintellij.settings";
    }

    @Override
    public String getDisplayName() {
        return "TestCraft";
    }

    @Override
    public @Nullable JComponent createComponent() {
        // Create a panel with a brief description of TestCraft settings
        JPanel panel = new JPanel();
        panel.add(new JLabel("<html><body style='width: 300px; padding: 10px;'>" +
                "<h2>TestCraft Settings</h2>" +
                "<p>Configure various aspects of TestCraft:</p>" +
                "<ul>" +
                "<li><b>Test Annotations</b> - Configure test case annotation schema and validation</li>" +
                "<li><b>Assert Validation</b> - Set up rules for validating test assertions</li>" +
                "<li><b>LLM Settings</b> - Configure Ollama LLM integration for test suggestions</li>" +
                "</ul>" +
                "</body></html>"));
        return panel;
    }

    @Override
    public boolean hasOwnContent() {
        return true;
    }

    @Override
    public boolean isModified() {
        return false; // This is just a container, no settings to modify
    }

    @Override
    public void apply() {
        // No settings to apply in the parent
    }

    @Override
    public Configurable @NotNull [] getConfigurables() {
        return new Configurable[0]; // Child configurables are defined in plugin.xml
    }
} 