package com.github.jaksonlin.pitestintellij.settings;

import com.github.jaksonlin.pitestintellij.services.InvalidAssertionConfigService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class InvalidAssertSettingsConfigurable implements Configurable {
    private InvalidAssertSettingsComponent settingsComponent;
    private final InvalidAssertionConfigService configService = ApplicationManager.getApplication().getService(InvalidAssertionConfigService.class);

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Assert Validation";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new InvalidAssertSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        InvalidAssertionConfigService.State state = configService.getState();
        if (state == null) return false;

        return !settingsComponent.getEditorText().equals(state.invalidAssertionText) ||
               settingsComponent.isEnableInvalidAssertCheck() != state.enable;
    }

    @Override
    public void apply() {
        InvalidAssertionConfigService.State state = configService.getState();
        if (state == null) return;

        String assertConfigLines = settingsComponent.getEditorText();
        // check if the input is \n separated, if not, split by \r\n
        if (assertConfigLines.contains("\n")) {
            state.invalidAssertionText = assertConfigLines;
        } else {
            state.invalidAssertionText = assertConfigLines.replace("\r\n", "\n");
        }

        state.enable = settingsComponent.isEnableInvalidAssertCheck();
    }

    @Override
    public void reset() {
        InvalidAssertionConfigService.State state = configService.getState();
        if (state == null) return;

        settingsComponent.setEditorText(state.invalidAssertionText);
        settingsComponent.setEnableInvalidAssertCheck(state.enable);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 