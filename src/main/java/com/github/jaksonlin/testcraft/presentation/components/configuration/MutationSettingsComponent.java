package com.github.jaksonlin.testcraft.presentation.components.configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.github.jaksonlin.testcraft.infrastructure.services.config.MutationConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;

public class MutationSettingsComponent {
    private final JPanel mainPanel;
    private final ComboBox<String> mutatorGroupComboBox;

    // Dependency Directories UI
    private final DefaultListModel<String> dependencyDirsModel = new DefaultListModel<>();
    private final JList<String> dependencyDirsList = new JList<>(dependencyDirsModel);

    // First-Load JAR Patterns UI
    private final DefaultListModel<String> firstLoadJarsModel = new DefaultListModel<>();
    private final JList<String> firstLoadJarsList = new JList<>(firstLoadJarsModel);

    public MutationSettingsComponent() {
        mutatorGroupComboBox = new ComboBox<>(new String[]{"DEFAULTS", "STRONGER", "STARTER_KIT"});
        mutatorGroupComboBox.setSelectedItem(MutationConfigService.getInstance().getMutatorGroup());

        // Load initial values
        for (String dir : MutationConfigService.getInstance().getDependencyDirectoriesOrder().split(";")) {
            if (!dir.trim().isEmpty()) dependencyDirsModel.addElement(dir.trim());
        }
        for (String pattern : MutationConfigService.getInstance().getFirstLoadDependentJars().split(";")) {
            if (!pattern.trim().isEmpty()) firstLoadJarsModel.addElement(pattern.trim());
        }

        // Dependency Directories Panel
        JPanel depDirsPanel = createEditableListPanel(
            I18nService.getInstance().message("settings.mutation.dependency.directories.order.label"),
            dependencyDirsList, dependencyDirsModel);

        // First-Load JAR Patterns Panel
        JPanel firstLoadJarsPanel = createEditableListPanel(
            I18nService.getInstance().message("settings.mutation.first.load.jar.patterns.label"),
            firstLoadJarsList, firstLoadJarsModel);

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(I18nService.getInstance().message("settings.mutation.default.mutator.group.label")), mutatorGroupComboBox)
                .addComponent(depDirsPanel)
                .addComponent(firstLoadJarsPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JPanel createEditableListPanel(String label, JList<String> list, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBLabel(label), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(list);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton(I18nService.getInstance().message("settings.mutation.button.add"));
        JButton removeButton = new JButton(I18nService.getInstance().message("settings.mutation.button.remove"));

        addButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, I18nService.getInstance().message("settings.mutation.dialog.enter.value"));
            if (input != null && !input.trim().isEmpty()) {
                model.addElement(input.trim());
            }
        });

        removeButton.addActionListener(e -> {
            int selected = list.getSelectedIndex();
            if (selected != -1) {
                model.remove(selected);
            }
        });

        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return panel;
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

    // New: Getters for the dependency settings
    public String getDependencyDirectoriesOrder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dependencyDirsModel.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(dependencyDirsModel.get(i));
        }
        return sb.toString();
    }

    public String getFirstLoadDependentJars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < firstLoadJarsModel.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(firstLoadJarsModel.get(i));
        }
        return sb.toString();
    }

    // New: Setters for loading from config if needed
    public void setDependencyDirectoriesOrder(String dirs) {
        dependencyDirsModel.clear();
        for (String dir : dirs.split(";")) {
            if (!dir.trim().isEmpty()) dependencyDirsModel.addElement(dir.trim());
        }
    }

    public void setFirstLoadDependentJars(String patterns) {
        firstLoadJarsModel.clear();
        for (String pattern : patterns.split(";")) {
            if (!pattern.trim().isEmpty()) firstLoadJarsModel.addElement(pattern.trim());
        }
    }
}