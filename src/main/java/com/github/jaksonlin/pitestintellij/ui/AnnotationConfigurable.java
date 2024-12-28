package com.github.jaksonlin.pitestintellij.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema;
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class AnnotationConfigurable implements Configurable {
    private EditorTextField editor;
    private JTextField packageTextField;
    private JCheckBox autoImportCheckbox;
    private final AnnotationConfigService configService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);
    private final ObjectMapper jsonMapper = JsonMapper.builder().build();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Test Annotation Configuration";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JBPanel<?> mainPanel = new JBPanel<>(new VerticalLayout(10));

        // Import Settings Section (now at top)
        mainPanel.add(createImportSettingsPanel());

        // Schema Editor Section
        mainPanel.add(new JBLabel("Annotation Schema:"));
        mainPanel.add(createSchemaEditor());

        // Buttons Panel
        mainPanel.add(createButtonsPanel());

        // Help Panel
        mainPanel.add(createHelpPanel());

        return mainPanel;
    }

    private JComponent createSchemaEditor() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        AnnotationConfigService.State state = configService.getState();
        String schemaJson = (state != null) ? state.schemaJson : AnnotationSchema.DEFAULT_SCHEMA;
        Document document = EditorFactory.getInstance().createDocument(schemaJson);
        editor = new EditorTextField(document, project, JsonFileType.INSTANCE, false, false);
        editor.setOneLineMode(false);
        editor.setPreferredWidth(400); // Set a fixed preferred size
        editor.addSettingsProvider(editor -> {
            EditorSettings settings = editor.getSettings();
            settings.setLineNumbersShown(true);
            settings.setWhitespacesShown(true);
            settings.setUseSoftWraps(true);
        });

        return new JBScrollPane(editor);
    }

    private JComponent createImportSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        // Package Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JBLabel("Annotation Package:"), gbc);

        // Package TextField
        packageTextField = new JTextField(configService.getAnnotationPackage());
        packageTextField.setPreferredSize(new Dimension(200, packageTextField.getPreferredSize().height));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(packageTextField, gbc);

        // Auto Import Checkbox
        autoImportCheckbox = new JCheckBox("Auto Import", configService.isAutoImport());
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.insets.left = 20;
        panel.add(autoImportCheckbox, gbc);

        return panel;
    }

    private JComponent createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton restoreDefaultsButton = new JButton("Restore Defaults");
        restoreDefaultsButton.addActionListener(e -> {
            try {
                editor.setText(AnnotationSchema.DEFAULT_SCHEMA);
                packageTextField.setText("com.example.unittest.annotations");
                autoImportCheckbox.setSelected(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Failed to restore defaults: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(restoreDefaultsButton);
        return panel;
    }

    private JComponent createHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea helpText = new JTextArea("""
        Define your test annotation schema in JSON format.
            Available field types: STRING, STRING_LIST

            Package: The base package for your annotations
            Auto Import: Automatically add import statements when generating annotations
        """.trim());
        helpText.setWrapStyleWord(true);
        helpText.setLineWrap(true);
        helpText.setOpaque(false);
        helpText.setEditable(false);
        helpText.setFocusable(false);
        helpText.setBackground(UIManager.getColor("Label.background"));
        helpText.setFont(UIManager.getFont("Label.font"));
        panel.add(helpText, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public boolean isModified() {
        AnnotationConfigService.State state = configService.getState();
        return (editor != null && !Objects.equals(editor.getText(), (state != null ? state.schemaJson : AnnotationSchema.DEFAULT_SCHEMA))) ||
                (packageTextField != null && !Objects.equals(packageTextField.getText(), configService.getAnnotationPackage())) ||
                (autoImportCheckbox != null && autoImportCheckbox.isSelected() != configService.isAutoImport());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (editor != null) {
            String jsonText = editor.getText();
            try {
                // Validate JSON format and schema
                jsonMapper.readValue(jsonText, AnnotationSchema.class);
                AnnotationConfigService.State state = configService.getState();
                if (state != null) {
                    state.schemaJson = jsonText;
                }

                // Update import settings
                if (packageTextField != null) {
                    configService.setAnnotationPackage(packageTextField.getText());
                }
                if (autoImportCheckbox != null) {
                    configService.setAutoImport(autoImportCheckbox.isSelected());
                }

            } catch (IOException e) {
                throw new ConfigurationException("Invalid JSON schema: " + e.getMessage());
            }
        }
    }

    @Override
    public void reset() {
        AnnotationConfigService.State state = configService.getState();
        editor.setText(state != null ? state.schemaJson : AnnotationSchema.DEFAULT_SCHEMA);
        packageTextField.setText(configService.getAnnotationPackage());
        autoImportCheckbox.setSelected(configService.isAutoImport());
    }

    @Override
    public void disposeUIResources() {
        editor = null;
        packageTextField = null;
        autoImportCheckbox = null;
    }
}