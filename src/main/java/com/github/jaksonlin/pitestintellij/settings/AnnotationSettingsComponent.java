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
    private final JBTextField packageTextField = new JBTextField();
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
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0;

        // Import Settings section
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.0;
        JLabel importSettingsLabel = new JBLabel("<html><body><b>Import Settings</b></body></html>");
        contentPanel.add(importSettingsLabel, c);

        // Package input
        addLabelAndField(contentPanel, "Annotation Package:", packageTextField, 1,
                "Package name for test annotations (e.g. com.example.unittest.annotations)");

        // Checkboxes
        c.gridy = 2;
        c.gridwidth = 2;
        autoImportCheckBox = new JCheckBox("Auto Import Annotations");
        autoImportCheckBox.setToolTipText("Automatically import test annotations when needed");
        contentPanel.add(autoImportCheckBox, c);

        c.gridy = 3;
        enableValidationCheckBox = new JCheckBox("Enable Annotation Validation");
        enableValidationCheckBox.setToolTipText("Validate test annotations against the schema");
        contentPanel.add(enableValidationCheckBox, c);

        // Schema Configuration section
        c.gridy = 4;
        c.insets = new Insets(15, 5, 5, 5);
        JLabel schemaLabel = new JBLabel("<html><body><b>Schema Configuration</b></body></html>");
        contentPanel.add(schemaLabel, c);

        // Schema editor
        c.gridy = 5;
        c.weighty = 1.0; // Make editor expand vertically
        c.insets = new Insets(5, 5, 5, 5);
        schemaEditor = createSchemaEditor();
        
        // Wrap editor in a panel to ensure it expands properly
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(new JBLabel("JSON Schema:"), BorderLayout.NORTH);
        editorPanel.add(schemaEditor, BorderLayout.CENTER);
        contentPanel.add(editorPanel, c);

        // Schema help text
        c.gridy = 6;
        c.weighty = 0.0; // Reset vertical weight
        c.insets = new Insets(10, 5, 5, 5);
        JLabel helpText = new JBLabel("<html><body style='width: 300px'>" +
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

    private EditorTextField createSchemaEditor() {
        EditorTextField editor = new EditorTextField("", null, JsonFileType.INSTANCE) {
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
                editor.getSettings().setAdditionalLinesCount(3);
                editor.getSettings().setAdditionalColumnsCount(3);
                editor.getSettings().setRightMarginShown(true);
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

    private void addLabelAndField(JPanel panel, String labelText, JComponent field, int gridy, String tooltip) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;

        // Label
        c.gridx = 0;
        c.gridy = gridy;
        c.gridwidth = 1;
        c.weightx = 0.0;
        JLabel label = new JBLabel(labelText);
        label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));
        panel.add(label, c);

        // Field
        c.gridx = 1;
        c.weightx = 1.0;
        field.setToolTipText(tooltip);
        panel.add(field, c);
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