package com.github.jaksonlin.testcraft.presentation.components.configuration;

import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.awt.*;

public class InvalidTestCaseSettingsComponent {
    private final JPanel mainPanel;
    private final EditorTextField assertionEditor;
    private final JBCheckBox enableCheckbox;
    private final JBCheckBox enableCommentCheckbox;
    private static final int EDITOR_HEIGHT = 200;

    public InvalidTestCaseSettingsComponent() {
        // Create main panel with a border layout to ensure full width usage
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create content panel with GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0;

        // Validation Settings section
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.0;
        JLabel validationLabel = new JBLabel("<html><body><b>" + I18nService.getInstance().message("settings.invalidTestCase.title") + "</b></body></html>");
        contentPanel.add(validationLabel, c);

        // Checkboxes
        c.gridy = 1;
        enableCheckbox = new JBCheckBox(I18nService.getInstance().message("settings.invalidTestCase.enableCheck"));
        enableCheckbox.setToolTipText(I18nService.getInstance().message("settings.invalidTestCase.enableCheck.tooltip"));
        contentPanel.add(enableCheckbox, c);

        c.gridy = 2;
        enableCommentCheckbox = new JBCheckBox(I18nService.getInstance().message("settings.invalidTestCase.enableCommentCheck"));
        enableCommentCheckbox.setToolTipText(I18nService.getInstance().message("settings.invalidTestCase.enableCommentCheck.tooltip"));
        contentPanel.add(enableCommentCheckbox, c);

        // Invalid Assertions section
        c.gridy = 3;
        c.insets = new Insets(15, 5, 5, 5);
        JLabel assertionsLabel = new JBLabel("<html><body><b>" + I18nService.getInstance().message("settings.invalidTestCase.assertions.title") + "</b></body></html>");
        contentPanel.add(assertionsLabel, c);

        // Editor description
        c.gridy = 4;
        c.insets = new Insets(5, 5, 5, 5);
        JLabel editorDesc = new JBLabel(I18nService.getInstance().message("settings.invalidTestCase.assertions.description"));
        contentPanel.add(editorDesc, c);

        // Assertion editor
        c.gridy = 5;
        c.weighty = 1.0; // Make editor expand vertically
        assertionEditor = createAssertionEditor();
        contentPanel.add(assertionEditor, c);

        // Help text
        c.gridy = 6;
        c.weighty = 0.0; // Reset vertical weight
        c.insets = new Insets(10, 5, 5, 5);
        JLabel helpText = new JBLabel("<html><body style='width: 300px'>" +
                "<b>" + I18nService.getInstance().message("settings.invalidTestCase.assertions.examples.title") + "</b><br>" +
                I18nService.getInstance().message("settings.invalidTestCase.assertions.examples.1") + "<br>" +
                I18nService.getInstance().message("settings.invalidTestCase.assertions.examples.2") + "<br>" +
                I18nService.getInstance().message("settings.invalidTestCase.assertions.examples.3") + "<br>" +
                I18nService.getInstance().message("settings.invalidTestCase.assertions.examples.4") +
                "</body></html>");
        contentPanel.add(helpText, c);

        // Add content panel to main panel
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private EditorTextField createAssertionEditor() {
        EditorTextField editor = new EditorTextField() {
            @Override
            protected @NotNull EditorEx createEditor() {
                EditorEx editor = (EditorEx) super.createEditor();
                editor.setVerticalScrollbarVisible(true);
                editor.setHorizontalScrollbarVisible(true);
                
                EditorSettings settings = editor.getSettings();
                settings.setFoldingOutlineShown(true);
                settings.setLineNumbersShown(true);
                settings.setLineMarkerAreaShown(true);
                settings.setIndentGuidesShown(true);
                settings.setUseSoftWraps(false);
                settings.setAdditionalLinesCount(3);
                settings.setAdditionalColumnsCount(3);
                settings.setRightMarginShown(true);
                
                return editor;
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, EDITOR_HEIGHT);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(100, EDITOR_HEIGHT);
            }
        };
        editor.setOneLineMode(false);
        return editor;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return assertionEditor;
    }

    public boolean isEnableCheck() {
        return enableCheckbox.isSelected();
    }

    public void setEnableCheck(boolean selected) {
        enableCheckbox.setSelected(selected);
    }

    public boolean isEnableCommentCheck() {
        return enableCommentCheckbox.isSelected();
    }

    public void setEnableCommentCheck(boolean selected) {
        enableCommentCheckbox.setSelected(selected);
    }

    public String getInvalidAssertionText() {
        return assertionEditor.getText();
    }

    public void setInvalidAssertionText(String text) {
        assertionEditor.setText(text);
    }
} 