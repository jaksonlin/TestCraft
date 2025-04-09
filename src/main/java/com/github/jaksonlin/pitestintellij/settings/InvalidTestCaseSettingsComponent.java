package com.github.jaksonlin.pitestintellij.settings;

import com.github.jaksonlin.pitestintellij.services.InvalidTestCaseConfigService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class InvalidTestCaseSettingsComponent {
    private final JPanel mainPanel;
    private final EditorTextField assertionEditor;
    private final JBCheckBox enableCheckbox;
    private final JBCheckBox enableCommentCheckbox;
    private final JBCheckBox copyAsMarkdownCheckbox;
    private final JBCheckBox copyPromptCheckbox;
    private static final int EDITOR_HEIGHT = 200;

    public InvalidTestCaseSettingsComponent() {
        // Create checkboxes with descriptions
        enableCheckbox = new JBCheckBox("Enable invalid assertion check");
        enableCheckbox.setToolTipText("When enabled, test methods will be checked for invalid assertion patterns");
        
        enableCommentCheckbox = new JBCheckBox("Enable test step comment check");
        enableCommentCheckbox.setToolTipText("When enabled, test methods will be checked for descriptive comments");

        copyAsMarkdownCheckbox = new JBCheckBox("Copy LLM response as markdown");
        copyAsMarkdownCheckbox.setToolTipText("When enabled, LLM responses will be copied in markdown format");

        copyPromptCheckbox = new JBCheckBox("Copy prompt to clipboard");
        copyPromptCheckbox.setToolTipText("When enabled, allows copying the prompt sent to LLM for external services");

        // Create editor for invalid assertions
        assertionEditor = new EditorTextField() {
            @Override
            protected @NotNull EditorEx createEditor() {
                EditorEx editor = (EditorEx) super.createEditor();
                editor.setVerticalScrollbarVisible(true);
                editor.setHorizontalScrollbarVisible(true);
                EditorSettings settings = editor.getSettings();
                settings.setLineNumbersShown(true);
                settings.setIndentGuidesShown(true);
                return editor;
            }
        };
        assertionEditor.setPreferredSize(new Dimension(-1, EDITOR_HEIGHT));

        // Help text for invalid assertions
        String helpText = "<html><b>Examples of invalid assertions that will be flagged:</b><br>" +
                "• assertTrue(true) - trivial assertion<br>" +
                "• assertEquals(1, 1) - comparing same values<br>" +
                "• assertNotNull(new Object()) - testing newly created object<br>" +
                "• assertEquals(\"success\", \"success\") - comparing identical strings</html>";

        // Create a panel for LLM-related settings
        JPanel llmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        llmPanel.add(new JBLabel("<html><b>LLM Integration Settings</b></html>"));

        // Build the layout using FormBuilder for consistent spacing and organization
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(new JBLabel("<html><b>Test Case Validation Settings</b></html>"))
                .addVerticalGap(10)
                .addComponent(enableCheckbox)
                .addComponent(enableCommentCheckbox)
                .addVerticalGap(20)
                .addComponent(llmPanel)
                .addVerticalGap(5)
                .addComponent(copyAsMarkdownCheckbox)
                .addComponent(copyPromptCheckbox)
                .addVerticalGap(20)
                .addComponent(new JBLabel("Invalid Assertion Patterns"))
                .addComponent(new JBLabel("Enter patterns for assertions that should be flagged as invalid (one per line):"))
                .addComponent(assertionEditor)
                .addComponent(new JBLabel(helpText))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        mainPanel.setBorder(JBUI.Borders.empty(10));
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

    public boolean isCopyAsMarkdown() {
        return copyAsMarkdownCheckbox.isSelected();
    }

    public void setCopyAsMarkdown(boolean selected) {
        copyAsMarkdownCheckbox.setSelected(selected);
    }

    public boolean isCopyPrompt() {
        return copyPromptCheckbox.isSelected();
    }

    public void setCopyPrompt(boolean selected) {
        copyPromptCheckbox.setSelected(selected);
    }

    public String getInvalidAssertionText() {
        return assertionEditor.getText();
    }

    public void setInvalidAssertionText(String text) {
        assertionEditor.setText(text);
    }
} 