package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InvalidAssertSettingsComponent {
    private final JPanel mainPanel;
    private final EditorTextField editor;
    private final JCheckBox enableInvalidAssertCheckbox;
    private static final int EDITOR_HEIGHT = 300; // Fixed height for the editor

    public InvalidAssertSettingsComponent() {
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

        // Enable/disable checkbox
        enableInvalidAssertCheckbox = new JCheckBox("Enable Invalid Assertion Check");
        enableInvalidAssertCheckbox.setToolTipText("Enable checking for invalid or trivial assertions in test methods");
        contentPanel.add(enableInvalidAssertCheckbox, c);

        // Add some vertical spacing
        c.insets = new Insets(10, 0, 10, 0);
        contentPanel.add(Box.createVerticalStrut(1), c);

        // Description label
        JBLabel descriptionLabel = new JBLabel("<html><body style='width: 100%'>" +
                "Enter invalid assertion patterns (one per line):<br>" +
                "These patterns will be flagged as invalid when found in test methods." +
                "</body></html>");
        contentPanel.add(descriptionLabel, c);

        // Create text editor with Java code highlighting
        FileType javaFileType = FileTypeManager.getInstance().getFileTypeByExtension("java");
        editor = new EditorTextField("", null, javaFileType) {
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
                // Use fixed height while allowing width to be flexible
                return new Dimension(super.getPreferredSize().width, EDITOR_HEIGHT);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(100, EDITOR_HEIGHT);
            }
        };
        editor.setOneLineMode(false);

        // Wrap editor in a panel to ensure it expands properly
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(editor, BorderLayout.CENTER);
        c.fill = GridBagConstraints.HORIZONTAL; // Changed from BOTH to HORIZONTAL
        c.weighty = 0.0; // Remove vertical expansion
        contentPanel.add(editorPanel, c);

        // Add help text
        c.insets = new Insets(10, 0, 5, 0); // Add some space above help text
        JLabel helpText = new JBLabel("<html><body style='width: 100%'>" +
                "<p><b>Examples of invalid assertions:</b></p>" +
                "<ul>" +
                "<li><code>assertTrue(true)</code> - Always true</li>" +
                "<li><code>assertEquals(1, 1)</code> - Trivial comparison</li>" +
                "<li><code>assertNotNull(\"string\")</code> - Literal can't be null</li>" +
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
        return editor;
    }

    @NotNull
    public String getEditorText() {
        return editor.getText();
    }

    public void setEditorText(@NotNull String text) {
        editor.setText(text);
    }

    public boolean isEnableInvalidAssertCheck() {
        return enableInvalidAssertCheckbox.isSelected();
    }

    public void setEnableInvalidAssertCheck(boolean selected) {
        enableInvalidAssertCheckbox.setSelected(selected);
    }
} 