package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AnnotationSettingsComponent {
    private final JPanel mainPanel;
    private final EditorTextField schemaEditor;
    private final JBTextField packageTextField;
    private final JCheckBox autoImportCheckBox;
    private final JCheckBox enableValidationCheckBox;
    private static final int EDITOR_HEIGHT = 300;

    public AnnotationSettingsComponent() {
        // Create main panel with a border layout to ensure full width usage
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create content panel with GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0; // Make components expand horizontally

        // Import Settings section
        JLabel importSettingsLabel = new JBLabel("<html><body><b>Import Settings</b></body></html>");
        contentPanel.add(importSettingsLabel, c);

        // Package input
        JPanel packagePanel = new JPanel(new BorderLayout());
        packagePanel.add(new JBLabel("Annotation Package:"), BorderLayout.WEST);
        packageTextField = new JBTextField();
        packageTextField.setToolTipText("Package name for test annotations (e.g. com.example.unittest.annotations)");
        packagePanel.add(packageTextField, BorderLayout.CENTER);
        contentPanel.add(packagePanel, c);

        // Checkboxes
        autoImportCheckBox = new JCheckBox("Auto Import Annotations");
        autoImportCheckBox.setToolTipText("Automatically import test annotations when needed");
        contentPanel.add(autoImportCheckBox, c);

        enableValidationCheckBox = new JCheckBox("Enable Annotation Validation");
        enableValidationCheckBox.setToolTipText("Validate test annotations against the schema");
        contentPanel.add(enableValidationCheckBox, c);

        // Schema Configuration section
        c.insets = new Insets(15, 0, 5, 0);
        JLabel schemaLabel = new JBLabel("<html><body><b>Schema Configuration</b></body></html>");
        contentPanel.add(schemaLabel, c);

        c.insets = new Insets(5, 0, 5, 0);
        schemaEditor = new EditorTextField("", null, JsonFileType.INSTANCE) {
            @Override
            protected @NotNull EditorEx createEditor() {
                EditorEx editor = super.createEditor();
                editor.setVerticalScrollbarVisible(true);
                editor.setHorizontalScrollbarVisible(true);
                
                editor.getSettings().setFoldingOutlineShown(true);
                editor.getSettings().setLineNumbersShown(true);
                editor.getSettings().setLineMarkerAreaShown(true);
                editor.getSettings().setIndentGuidesShown(true);
                editor.getSettings().setUseSoftWraps(false);
                
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
        schemaEditor.setOneLineMode(false);

        // Wrap editor in a panel to ensure it expands properly
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(schemaEditor, BorderLayout.CENTER);
        contentPanel.add(editorPanel, c);

        // Schema help text
        c.insets = new Insets(10, 0, 5, 0);
        JLabel helpText = new JBLabel("<html><body style='width: 100%'>" +
                "<p><b>Schema Format:</b></p>" +
                "<ul>" +
                "<li>Define fields with name, type (STRING/STRING_LIST), and validation rules</li>" +
                "<li>Specify required fields and default values</li>" +
                "</ul>" +
                "</body></html>");
        contentPanel.add(helpText, c);

        // Add content panel to main panel
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return schemaEditor;
    }

    @NotNull
    public String getSchemaText() {
        return schemaEditor.getText();
    }

    public void setSchemaText(@NotNull String text) {
        schemaEditor.setText(text);
    }

    @NotNull
    public String getPackageText() {
        return packageTextField.getText();
    }

    public void setPackageText(@NotNull String text) {
        packageTextField.setText(text);
    }

    public boolean isAutoImport() {
        return autoImportCheckBox.isSelected();
    }

    public void setAutoImport(boolean selected) {
        autoImportCheckBox.setSelected(selected);
    }

    public boolean isEnableValidation() {
        return enableValidationCheckBox.isSelected();
    }

    public void setEnableValidation(boolean selected) {
        enableValidationCheckBox.setSelected(selected);
    }
} 