package com.github.jaksonlin.testcraft.settings;

import com.github.jaksonlin.testcraft.services.LLMService;
import com.github.jaksonlin.testcraft.MyBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OllamaSettingsConfigurable implements Configurable {
    private OllamaSettingsComponent settingsComponent = new OllamaSettingsComponent();
    private final LLMService llmService = ApplicationManager.getApplication().getService(LLMService.class);
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return MyBundle.message("settings.testcraft.llm");
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
        if (settingsComponent == null) {
            return false;
        }
        LLMService.State state = llmService.getState();
        boolean modified = !settingsComponent.getHostText().equals(state.ollamaHost);
        modified |= !settingsComponent.getPortText().equals(String.valueOf(state.ollamaPort));
        modified |= !settingsComponent.getModelText().equals(state.ollamaModel);
        modified |= !settingsComponent.getMaxTokensText().equals(String.valueOf(state.maxTokens));
        modified |= !settingsComponent.getTemperatureText().equals(String.valueOf(state.temperature));
        modified |= !settingsComponent.getTimeoutText().equals(String.valueOf(state.requestTimeout));
        modified |= settingsComponent.getCopyAsMarkdown() != state.copyAsMarkdown;
        return modified;
    }

    @Override
    public void apply() {
        try {
            LLMService.State state = llmService.getState();
            state.ollamaHost = settingsComponent.getHostText();
            state.ollamaPort = Integer.parseInt(settingsComponent.getPortText());
            state.maxTokens = Integer.parseInt(settingsComponent.getMaxTokensText());
            state.temperature = Float.parseFloat(settingsComponent.getTemperatureText());
            state.requestTimeout = Integer.parseInt(settingsComponent.getTimeoutText());
            state.ollamaModel = settingsComponent.getModelText();
            state.copyAsMarkdown = settingsComponent.getCopyAsMarkdown();

            llmService.loadState(state);
        } catch (NumberFormatException e) {
            // Handle invalid number format
            throw new IllegalStateException("Invalid number format in settings", e);
        }
    }

    @Override
    public void reset() {
        // Reset the settings component with the current state of the LLMService
        LLMService.State state = llmService.getState();
        settingsComponent.setHostText(state.ollamaHost);
        settingsComponent.setPortText(String.valueOf(state.ollamaPort));
        settingsComponent.setModelText(state.ollamaModel);
        settingsComponent.setMaxTokensText(String.valueOf(state.maxTokens));
        settingsComponent.setTemperatureText(String.valueOf(state.temperature));
        settingsComponent.setTimeoutText(String.valueOf(state.requestTimeout));
        settingsComponent.setCopyAsMarkdown(state.copyAsMarkdown);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 