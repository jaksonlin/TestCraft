package com.github.jaksonlin.testcraft.application.settings;

import com.alibaba.fastjson.JSON;
import com.github.jaksonlin.testcraft.domain.annotations.AnnotationSchema;
import com.github.jaksonlin.testcraft.infrastructure.services.config.AnnotationConfigService;
import com.github.jaksonlin.testcraft.presentation.components.configuration.AnnotationSettingsComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class AnnotationSettingsConfigurable implements Configurable {
    private AnnotationSettingsComponent settingsComponent;
    private final AnnotationConfigService configService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);

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
        try {
            String jsonText = settingsComponent.getSchemaText();
            // Validate JSON format
            JSON.parseObject(jsonText, AnnotationSchema.class);
            
            AnnotationConfigService.State state = configService.getState();
            if (state != null) {
                configService.setShouldCheckAnnotation(settingsComponent.isEnableValidation());
                configService.setAutoImport(settingsComponent.isAutoImport());
                configService.setAnnotationPackage(settingsComponent.getPackageText());
                configService.setSchemaJson(jsonText);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Invalid JSON format: " + e.getMessage());
        }
    }

    @Override
    public void reset() {
        AnnotationConfigService.State state = configService.getState();
        if (state != null) {
            settingsComponent.setSchemaText(state.schemaJson);
            settingsComponent.setPackageText(state.annotationPackage);
            settingsComponent.setAutoImport(state.autoImport);
            settingsComponent.setEnableValidation(state.shouldCheckAnnotation);
        }
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
} 