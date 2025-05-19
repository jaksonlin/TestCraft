package com.github.jaksonlin.testcraft.presentation.components.configuration;

import javax.swing.*;

import com.github.jaksonlin.testcraft.infrastructure.services.config.MutationConfigService;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;

public class MutationSettingsComponent {
    private final JPanel mainPanel;
    private final ComboBox<String> mutatorGroupComboBox;

    public MutationSettingsComponent() {
        mutatorGroupComboBox = new ComboBox<>(new String[]{"DEFAULTS", "STRONGER", "STARTER_KIT"});
        mutatorGroupComboBox.setSelectedItem(MutationConfigService.getInstance().getMutatorGroup());

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Default Mutator Group:"), mutatorGroupComboBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public String getSelectedMutatorGroup() {
        return (String) mutatorGroupComboBox.getSelectedItem();
    }

    public void setSelectedMutatorGroup(String mutatorGroup) {
        mutatorGroupComboBox.setSelectedItem(mutatorGroup);
    }

}