package com.github.jaksonlin.pitestintellij.settings;

import com.github.jaksonlin.pitestintellij.services.InvalidTestCaseConfigService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InvalidTestCaseSettingsComponent {
    private final JPanel mainPanel;
    private final EditorTextField schemaEditor;
    private final JBCheckBox enableCheckbox;
    private final JBCheckBox enableCommentCheckbox;
    private static final int EDITOR_HEIGHT = 150; // Reduced height since we have more components
    private final InvalidTestCaseConfigService service = ApplicationManager.getApplication().getService(InvalidTestCaseConfigService.class);

    public InvalidTestCaseSettingsComponent() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx = 1.0;
        constraints.insets = JBUI.insets(5);

        // Section header for Test Case Validation
        JBLabel sectionLabel = new JBLabel("<html><b>Test Case Validation</b></html>");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        mainPanel.add(sectionLabel, constraints);

        // Enable invalid assertion check
        enableCheckbox = new JBCheckBox("Enable invalid assertion check");
        enableCheckbox.setToolTipText("When enabled, test methods will be checked for invalid assertion patterns");
        constraints.gridy = 1;
        mainPanel.add(enableCheckbox, constraints);

        // Enable comment check
        enableCommentCheckbox = new JBCheckBox("Enable test step comment check");
        enableCommentCheckbox.setToolTipText("When enabled, test methods will be checked for descriptive comments");
        constraints.gridy = 2;
        mainPanel.add(enableCommentCheckbox, constraints);

        // Description for invalid assertion patterns
        JBLabel descriptionLabel = new JBLabel("Enter invalid assertion patterns (one per line)");
        constraints.gridy = 3;
        mainPanel.add(descriptionLabel, constraints);

        // Pattern editor
        schemaEditor = new EditorTextField() {
            @Override
            protected EditorEx createEditor() {
                EditorEx editor = (EditorEx) super.createEditor();
                editor.setVerticalScrollbarVisible(true);
                editor.setHorizontalScrollbarVisible(true);
                EditorSettings settings = editor.getSettings();
                settings.setLineNumbersShown(true);
                settings.setIndentGuidesShown(true);
                return editor;
            }
        };
        schemaEditor.setPreferredSize(new Dimension(-1, EDITOR_HEIGHT));
        constraints.gridy = 4;
        mainPanel.add(schemaEditor, constraints);

        // Help text
        String helpText = "<html>Examples of invalid assertions:<br>" +
                "- assertTrue(true)<br>" +
                "- assertEquals(1, 1)<br>" +
                "- assertNotNull(new Object())</html>";
        JBLabel helpLabel = new JBLabel(helpText);
        constraints.gridy = 5;
        mainPanel.add(helpLabel, constraints);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return schemaEditor;
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
        return schemaEditor.getText();
    }

    public void setInvalidAssertionText(String text) {
        schemaEditor.setText(text);
    }
} 