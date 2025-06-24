package com.github.jaksonlin.testcraft.application.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import javax.swing.JComponent;

import com.github.jaksonlin.testcraft.infrastructure.services.config.MutationConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;

import com.github.jaksonlin.testcraft.presentation.components.configuration.MutationSettingsComponent;

public class MutationSettingsConfigurable implements Configurable {
    private MutationSettingsComponent mutationSettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return I18nService.getInstance().message("settings.testcraft.mutation");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mutationSettingsComponent = new MutationSettingsComponent();
        return mutationSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        return !mutationSettingsComponent.getSelectedMutatorGroup().equals(MutationConfigService.getInstance().getMutatorGroup())
        || !mutationSettingsComponent.getDependencyDirectoriesOrder().equals(MutationConfigService.getInstance().getDependencyDirectoriesOrder())
        || !mutationSettingsComponent.getFirstLoadDependentJars().equals(MutationConfigService.getInstance().getFirstLoadDependentJars());
    }

    @Override
    public void apply() throws ConfigurationException {
        MutationConfigService.getInstance().setMutatorGroup(mutationSettingsComponent.getSelectedMutatorGroup());
        MutationConfigService.getInstance().setDependencyDirectoriesOrder(mutationSettingsComponent.getDependencyDirectoriesOrder());
        MutationConfigService.getInstance().setFirstLoadDependentJars(mutationSettingsComponent.getFirstLoadDependentJars());
    }

    @Override
    public void reset() {
        mutationSettingsComponent.setSelectedMutatorGroup(MutationConfigService.getInstance().getMutatorGroup());
        mutationSettingsComponent.setDependencyDirectoriesOrder(MutationConfigService.getInstance().getDependencyDirectoriesOrder());
        mutationSettingsComponent.setFirstLoadDependentJars(MutationConfigService.getInstance().getFirstLoadDependentJars());
    }
} 