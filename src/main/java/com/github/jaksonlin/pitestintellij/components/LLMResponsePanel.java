package com.github.jaksonlin.pitestintellij.components;

import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.github.jaksonlin.pitestintellij.observers.LLMMessageObserver;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;

public class LLMResponsePanel extends JPanel implements BasicEventObserver {

    private final JEditorPane editorPane = new JEditorPane();

    public LLMResponsePanel() {
        this.setLayout(new BorderLayout());
        setupUI();
    }

    private void setupUI() {
        // Setup editor pane
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(JBUI.Borders.empty(2, 2));

        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(e -> copyToClipboard());
        toolbar.add(copyButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearContent());
        toolbar.add(clearButton);

        // Add components to main panel
        this.add(toolbar, BorderLayout.NORTH);
        this.add(new JBScrollPane(editorPane), BorderLayout.CENTER);
    }

    public void updateContent(String markdown) {
        String htmlContent = convertMarkdownToHtml(markdown);
        editorPane.setText(htmlContent);
        editorPane.setCaretPosition(0); // Scroll to top
    }

    private void copyToClipboard() {
        editorPane.selectAll();
        editorPane.copy();
        editorPane.select(0, 0); // Clear selection
    }

    private void clearContent() {
        editorPane.setText("");
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        return "<html><head><style>"
                + "body { font-family: monospace; font-size: 12pt; margin: 10px; }"
                + "pre { background-color: #f5f5f5; padding: 10px; border-radius: 4px; }"
                + "code { font-family: monospace; }"
                + "</style></head><body>"
                + html
                + "</body></html>";
    }

    @Override
    public void onEventHappen(Object eventObj) {
        String markdown = eventObj.toString();
        updateContent(markdown);
    }
}
