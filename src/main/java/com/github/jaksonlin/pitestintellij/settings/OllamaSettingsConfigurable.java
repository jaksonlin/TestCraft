package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OllamaSettingsConfigurable implements Configurable {
    private OllamaSettingsComponent settingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "LLM Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new OllamaSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        boolean modified = !settingsComponent.getHostText().equals(settings.ollamaHost);
        modified |= !settingsComponent.getPortText().equals(String.valueOf(settings.ollamaPort));
        modified |= !settingsComponent.getModelText().equals(settings.ollamaModel);
        modified |= !settingsComponent.getMaxTokensText().equals(String.valueOf(settings.maxTokens));
        modified |= !settingsComponent.getTemperatureText().equals(String.valueOf(settings.temperature));
        modified |= !settingsComponent.getTimeoutText().equals(String.valueOf(settings.requestTimeout));
        return modified;
    }

    @Override
    public void apply() {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        settings.ollamaHost = settingsComponent.getHostText();
        try {
            settings.ollamaPort = Integer.parseInt(settingsComponent.getPortText());
            settings.maxTokens = Integer.parseInt(settingsComponent.getMaxTokensText());
            settings.temperature = Float.parseFloat(settingsComponent.getTemperatureText());
            settings.requestTimeout = Integer.parseInt(settingsComponent.getTimeoutText());
        } catch (NumberFormatException e) {
            // Handle invalid number format
            throw new IllegalStateException("Invalid number format in settings", e);
        }
        settings.ollamaModel = settingsComponent.getModelText();
    }

    @Override
    public void reset() {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        settingsComponent.setHostText(settings.ollamaHost);
        settingsComponent.setPortText(String.valueOf(settings.ollamaPort));
        settingsComponent.setModelText(settings.ollamaModel);
        settingsComponent.setMaxTokensText(String.valueOf(settings.maxTokens));
        settingsComponent.setTemperatureText(String.valueOf(settings.temperature));
        settingsComponent.setTimeoutText(String.valueOf(settings.requestTimeout));
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 