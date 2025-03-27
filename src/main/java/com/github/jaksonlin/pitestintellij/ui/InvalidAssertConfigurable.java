package com.github.jaksonlin.pitestintellij.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema;
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService;
import com.github.jaksonlin.pitestintellij.services.InvalidAssertionConfigService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.IOException;
import java.util.Objects;

public class InvalidAssertConfigurable implements Configurable {
    private EditorTextField editor;
    private JCheckBox enableInvalidAssertCheckbox;
    private final InvalidAssertionConfigService configService = ApplicationManager.getApplication().getService(InvalidAssertionConfigService.class);

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Invalid Unit Test Assert Configuration";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create enable/disable checkbox
        enableInvalidAssertCheckbox = new JCheckBox("Enable Invalid Assertion Check");
        enableInvalidAssertCheckbox.setSelected(configService.isEnable());
        panel.add(enableInvalidAssertCheckbox);

        // Add some vertical spacing
        panel.add(Box.createVerticalStrut(10));

        // Create description label
        JLabel descriptionLabel = new JLabel("Enter invalid assertion patterns (one per line):");
        panel.add(descriptionLabel);

        // Create text editor
        editor = new EditorTextField();
        editor.setOneLineMode(false);
        editor.setPreferredSize(new Dimension(400, 200));

        // Set initial text from config
        InvalidAssertionConfigService.State state = configService.getState();
        String initialText = state != null ? state.invalidAssertionText : 
                            InvalidAssertionConfigService.getBuiltInInvalidAssertionText();
        editor.setText(initialText);

        panel.add(editor);

        return panel;
    }

    @Override
    public boolean isModified() {
        InvalidAssertionConfigService.State state = configService.getState();
        return (editor != null && !Objects.equals(editor.getText(), (state != null ? state.invalidAssertionText : InvalidAssertionConfigService.getBuiltInInvalidAssertionText()))) ||
                (enableInvalidAssertCheckbox != null && enableInvalidAssertCheckbox.isSelected() != configService.isEnable());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (editor == null){
            return;
        }
        String assertConfigLines = editor.getText();

        InvalidAssertionConfigService.State state = configService.getState();
        if (state != null) {
            // check if the input is \n separated, if not, split by \r\n
            if (assertConfigLines.contains("\n")) {
                state.invalidAssertionText = assertConfigLines;
            } else {
                state.invalidAssertionText = assertConfigLines.replace("\r\n", "\n");
            }
        }

        if (enableInvalidAssertCheckbox != null) {
            configService.setEnable(enableInvalidAssertCheckbox.isSelected());
        }
    }
}
