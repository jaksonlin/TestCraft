package com.github.jaksonlin.pitestintellij.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class LLMSuggestionsDialog extends DialogWrapper {
    private final Project project;
    private final String suggestions;
    private JEditorPane editorPane;

    public LLMSuggestionsDialog(Project project, String suggestions) {
        super(project);
        this.project = project;
        this.suggestions = suggestions;
        init();
        setTitle("Unit Test Suggestions");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(800, 600));
        
        // Create editor pane with HTML support
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Convert markdown to HTML
        String htmlContent = convertMarkdownToHtml(suggestions);
        editorPane.setText(htmlContent);
        
        // Add editor pane to a scroll pane
        JBScrollPane scrollPane = new JBScrollPane(editorPane);
        scrollPane.setBorder(JBUI.Borders.empty(5));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .build();
        String html = renderer.render(document);
        
        // Wrap the HTML content with basic styling
        return "<html><head><style>"
                + "body { font-family: monospace; font-size: 12pt; margin: 10px; }"
                + "pre { background-color: #f5f5f5; padding: 10px; border-radius: 4px; }"
                + "code { font-family: monospace; }"
                + "</style></head><body>"
                + html
                + "</body></html>";
    }

    @Override
    protected Action[] createActions() {
        // Create custom actions
        Action copyAction = new AbstractAction("Copy to Clipboard") {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection selection = new StringSelection(suggestions);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            }
        };

        // Return both the copy action and the default close action
        return new Action[]{copyAction, getOKAction()};
    }

    @Override
    public void show() {
        super.show();
    }
}
