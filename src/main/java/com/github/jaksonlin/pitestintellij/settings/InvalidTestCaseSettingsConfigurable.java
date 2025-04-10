package com.github.jaksonlin.pitestintellij.settings;

import com.github.jaksonlin.pitestintellij.services.InvalidTestCaseConfigService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class InvalidTestCaseSettingsConfigurable implements Configurable {
    private InvalidTestCaseSettingsComponent mySettingsComponent;
    private final InvalidTestCaseConfigService service = ApplicationManager.getApplication().getService(InvalidTestCaseConfigService.class);

    @Override
    public String getDisplayName() {
        return "Test Case Validation";
    }

    @Override
    public @Nullable JComponent createComponent() {
        mySettingsComponent = new InvalidTestCaseSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        InvalidTestCaseConfigService.State state = service.getState();
        return mySettingsComponent != null && state != null && (
            mySettingsComponent.isEnableCheck() != state.enable ||
            mySettingsComponent.isEnableCommentCheck() != state.enableCommentCheck ||
            !mySettingsComponent.getInvalidAssertionText().equals(state.invalidAssertionText)
        );
    }

    @Override
    public void apply() {
        if (mySettingsComponent != null) {
            InvalidTestCaseConfigService.State state = service.getState();
            if (state != null) {
                state.enable = mySettingsComponent.isEnableCheck();
                state.enableCommentCheck = mySettingsComponent.isEnableCommentCheck();
                state.invalidAssertionText = mySettingsComponent.getInvalidAssertionText();
            }
        }
    }

    @Override
    public void reset() {
        if (mySettingsComponent != null) {
            InvalidTestCaseConfigService.State state = service.getState();
            if (state != null) {
                mySettingsComponent.setEnableCheck(state.enable);
                mySettingsComponent.setEnableCommentCheck(state.enableCommentCheck);
                mySettingsComponent.setInvalidAssertionText(state.invalidAssertionText);
            }
        }
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
} 