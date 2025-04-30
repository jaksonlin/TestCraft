package com.github.jaksonlin.testcraft.infrastructure.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jaksonlin.testcraft.core.annotations.AnnotationSchema;
import com.github.jaksonlin.testcraft.core.services.AnnotationConfigService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class AnnotationSettingsConfigurable implements Configurable {
    private AnnotationSettingsComponent settingsComponent;
    private final AnnotationConfigService configService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);
    private final ObjectMapper jsonMapper = JsonMapper.builder().build();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Test Annotations";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new AnnotationSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AnnotationConfigService.State state = configService.getState();
        if (state == null) return false;

        return !Objects.equals(settingsComponent.getSchemaText(), state.schemaJson) ||
               !Objects.equals(settingsComponent.getPackageText(), state.annotationPackage) ||
               settingsComponent.isAutoImport() != state.autoImport ||
               settingsComponent.isEnableValidation() != state.shouldCheckAnnotation;
    }

    @Override
    public void apply() throws ConfigurationException {
        String jsonText = settingsComponent.getSchemaText();
        try {
            // Validate JSON format and schema
            jsonMapper.readValue(jsonText, AnnotationSchema.class);
            
            AnnotationConfigService.State state = configService.getState();
            if (state == null) return;

            state.schemaJson = jsonText;
            state.annotationPackage = settingsComponent.getPackageText();
            state.autoImport = settingsComponent.isAutoImport();
            state.shouldCheckAnnotation = settingsComponent.isEnableValidation();
        } catch (IOException e) {
            throw new ConfigurationException("Invalid JSON schema: " + e.getMessage());
        }
    }

    @Override
    public void reset() {
        AnnotationConfigService.State state = configService.getState();
        if (state == null) return;

        settingsComponent.setSchemaText(state.schemaJson);
        settingsComponent.setPackageText(state.annotationPackage);
        settingsComponent.setAutoImport(state.autoImport);
        settingsComponent.setEnableValidation(state.shouldCheckAnnotation);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 