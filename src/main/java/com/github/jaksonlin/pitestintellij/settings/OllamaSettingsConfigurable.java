package com.github.jaksonlin.pitestintellij.settings;

import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OllamaSettingsConfigurable implements Configurable {
    private OllamaSettingsComponent settingsComponent;
    private final LLMService llmService = ApplicationManager.getApplication().getService(LLMService.class);
    
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
        boolean modified = !settingsComponent.getHostText().equals(llmService.getOllamaHost());
        modified |= !settingsComponent.getPortText().equals(String.valueOf(llmService.getOllamaPort()));
        modified |= !settingsComponent.getModelText().equals(llmService.getOllamaModel());
        modified |= !settingsComponent.getMaxTokensText().equals(String.valueOf(llmService.getMaxTokens()));
        modified |= !settingsComponent.getTemperatureText().equals(String.valueOf(llmService.getTemperature()));
        modified |= !settingsComponent.getTimeoutText().equals(String.valueOf(llmService.getRequestTimeout()));
        modified |= settingsComponent.getCopyAsMarkdown() != llmService.getCopyAsMarkdown();
        return modified;
    }

    @Override
    public void apply() {
        try {
            llmService.setOllamaPort(Integer.parseInt(settingsComponent.getPortText()));
            llmService.setMaxTokens(Integer.parseInt(settingsComponent.getMaxTokensText()));
            llmService.setTemperature(Float.parseFloat(settingsComponent.getTemperatureText()));
            llmService.setRequestTimeout(Integer.parseInt(settingsComponent.getTimeoutText()));
        } catch (NumberFormatException e) {
            // Handle invalid number format
            throw new IllegalStateException("Invalid number format in settings", e);
        }
        llmService.setOllamaModel(settingsComponent.getModelText());
        llmService.setCopyAsMarkdown(settingsComponent.getCopyAsMarkdown());
    }

    @Override
    public void reset() {   
        settingsComponent.setHostText(llmService.getOllamaHost());
        settingsComponent.setPortText(String.valueOf(llmService.getOllamaPort()));
        settingsComponent.setModelText(llmService.getOllamaModel());
        settingsComponent.setMaxTokensText(String.valueOf(llmService.getMaxTokens()));
        settingsComponent.setTemperatureText(String.valueOf(llmService.getTemperature()));
        settingsComponent.setTimeoutText(String.valueOf(llmService.getRequestTimeout()));
        settingsComponent.setCopyAsMarkdown(llmService.getCopyAsMarkdown());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 