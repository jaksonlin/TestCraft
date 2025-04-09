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
        return "Ollama Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPanel();
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
        return !settingsComponent.getOllamaHost().equals(settings.ollamaHost)
                || settingsComponent.getOllamaPort() != settings.ollamaPort
                || !settingsComponent.getOllamaModel().equals(settings.ollamaModel)
                || settingsComponent.getMaxTokens() != settings.maxTokens
                || settingsComponent.getTemperature() != settings.temperature
                || settingsComponent.getRequestTimeout() != settings.requestTimeout;
    }

    @Override
    public void apply() {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        settings.ollamaHost = settingsComponent.getOllamaHost();
        settings.ollamaPort = settingsComponent.getOllamaPort();
        settings.ollamaModel = settingsComponent.getOllamaModel();
        settings.maxTokens = settingsComponent.getMaxTokens();
        settings.temperature = settingsComponent.getTemperature();
        settings.requestTimeout = settingsComponent.getRequestTimeout();
    }

    @Override
    public void reset() {
        OllamaSettingsState settings = OllamaSettingsState.getInstance();
        settingsComponent.setOllamaHost(settings.ollamaHost);
        settingsComponent.setOllamaPort(settings.ollamaPort);
        settingsComponent.setOllamaModel(settings.ollamaModel);
        settingsComponent.setMaxTokens(settings.maxTokens);
        settingsComponent.setTemperature(settings.temperature);
        settingsComponent.setRequestTimeout(settings.requestTimeout);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 