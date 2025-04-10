package com.github.jaksonlin.pitestintellij.components;

import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import com.intellij.util.ui.UIUtil;
import com.intellij.ui.JBColor;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMResponsePanel extends JPanel implements BasicEventObserver {
    private final JEditorPane editorPane = new JEditorPane();
    private Timer loadingTimer;
    private int loadingDots = 0;
    private boolean isLoading = false;
    private String lastMarkdown = "";
    private boolean copyAsMarkdown = false;  // Default value matching OllamaSettingsState
    
    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        switch (eventName) {
            case "CONFIG_CHANGE:copyAsMarkdown":
                copyAsMarkdown = (boolean) eventObj;
                break;
            case "CHAT_RESPONSE":
                updateSuggestionMarkdown(eventObj.toString());
                break;
            case "START_LOADING":
                startLoading();
                break;
            case "STOP_LOADING":
                stopLoading();
                break;
            case "DRY_RUN_PROMPT":
                updateDryRunPrompt(eventObj.toString());
                break;
            default:
                break;
        }
    }


    private String getCodeStyle() {
        boolean isDarkTheme = !JBColor.isBright();
        String backgroundColor = isDarkTheme ? "#2b2d30" : "#fafafa";
        String textColor = isDarkTheme ? "#bababa" : "#2b2b2b";
        String codeBackground = isDarkTheme ? "#1e1f22" : "#f6f8fa";
        String codeBorder = isDarkTheme ? "#1e1f22" : "#e1e4e8";
        String linkColor = isDarkTheme ? "#589df6" : "#2470B3";

        return String.format("""
            body { 
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; 
                font-size: 13pt; 
                margin: 10px; 
                line-height: 1.4;
                background-color: %s;
                color: %s;
            }
            h1, h2, h3 { color: %s; }
            pre { 
                background-color: %s; 
                padding: 16px; 
                border-radius: 6px; 
                margin: 1em 0; 
                border: 1px solid %s;
                overflow: auto;
            }
            code { 
                font-family: "JetBrains Mono", "Fira Code", Consolas, Monaco, "Courier New", monospace;
                font-size: 12pt;
                color: %s;
            }
            a { color: %s; }
            .keyword { color: %s; }
            .string { color: %s; }
            .comment { color: %s; font-style: italic; }
            .number { color: %s; }
            .annotation { color: %s; }
            .type { color: %s; }
            .method { color: %s; }
            .field { color: %s; }
            .constant { color: %s; }
            .package { color: %s; }
            """,
            backgroundColor, textColor, textColor, codeBackground, codeBorder, textColor, linkColor,
            isDarkTheme ? "#cc7832" : "#d73a49",  // keyword
            isDarkTheme ? "#6a8759" : "#032f62",  // string
            isDarkTheme ? "#808080" : "#6a737d",  // comment
            isDarkTheme ? "#6897bb" : "#005cc5",  // number
            isDarkTheme ? "#bbb529" : "#e36209",  // annotation
            isDarkTheme ? "#ffc66d" : "#6f42c1",  // type
            isDarkTheme ? "#ffc66d" : "#6f42c1",  // method
            isDarkTheme ? "#9876aa" : "#005cc5",  // field
            isDarkTheme ? "#9876aa" : "#005cc5",  // constant
            isDarkTheme ? "#a9b7c6" : "#22863a"   // package
        );
    }

    public LLMResponsePanel() {
        this.setLayout(new BorderLayout());
        setupUI();
    }

    private void setupUI() {
        // Setup editor pane
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        editorPane.setBackground(UIUtil.getPanelBackground());

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

    private String highlightJavaCode(String code) {
        // Replace special characters first
        code = code.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;");

        // Highlight keywords
        code = code.replaceAll("\\b(public|private|protected|class|interface|void|static|final|if|else|for|while|do|break|continue|return|try|catch|throw|throws|new|extends|implements|import|package|null|true|false|this|super|instanceof|synchronized|volatile|transient|native|strictfp|abstract|default)\\b", "<span class=\"keyword\">$1</span>");
        
        // Highlight strings (handling escaped quotes)
        code = code.replaceAll("\"((?:\\\\.|[^\"])*?)\"", "<span class=\"string\">\"$1\"</span>");
        
        // Highlight numbers (including decimals and negative numbers)
        code = code.replaceAll("\\b(-?\\d*\\.?\\d+)\\b", "<span class=\"number\">$1</span>");
        
        // Highlight annotations
        code = code.replaceAll("(@\\w+(?:\\.[\\w.]+)*)", "<span class=\"annotation\">$1</span>");
        
        // Highlight types (including generics)
        code = code.replaceAll("\\b(String|Integer|Boolean|Double|Float|List|Map|Set|Object|Exception|Error|Throwable|Class|Void|Character|Byte|Short|Long|Thread|Runnable|Override)(?:<[^>]+>)?\\b", "<span class=\"type\">$1</span>");
        
        // Highlight method declarations
        code = code.replaceAll("(?<=\\s)(\\w+)\\s*\\(", "<span class=\"method\">$1</span>(");
        
        // Highlight single-line comments
        code = code.replaceAll("(//[^\n]*)", "<span class=\"comment\">$1</span>");
        
        // Highlight multi-line comments
        code = code.replaceAll("/\\*(.*?)\\*/", "<span class=\"comment\">/*$1*/</span>");

        return code;
    }

    private void startLoading() {
        if (loadingTimer != null) {
            loadingTimer.cancel();
        }
        isLoading = true;
        loadingDots = 0;
        loadingTimer = new Timer();
        loadingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isLoading) {
                    loadingTimer.cancel();
                    return;
                }
                loadingDots = (loadingDots + 1) % 4;
                String dots = ".".repeat(loadingDots);
                String loadingText = String.format("Generating suggestions%s", dots);

                editorPane.setText(loadingText);
            }
        }, 0, 500); // Update every 500ms
    }

    private void stopLoading() {
        isLoading = false;
        if (loadingTimer != null) {
            loadingTimer.cancel();
            loadingTimer = null;
        }
    }

    private void updateSuggestionMarkdown(String markdown) {
        lastMarkdown = markdown;
        String htmlContent = convertMarkdownToHtml(markdown);
        editorPane.setText(htmlContent);
        editorPane.setCaretPosition(0); // Scroll to top
    }

    private void copyToClipboard() {
        String contentToCopy;
        if (copyAsMarkdown) {
            contentToCopy = lastMarkdown;
        } else {
            contentToCopy = editorPane.getText();
        }
        
        StringSelection selection = new StringSelection(contentToCopy);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    private void clearContent() {
        editorPane.setText("");
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        // Extract code blocks and highlight them
        Pattern codePattern = Pattern.compile("<pre><code>(.*?)</code></pre>", Pattern.DOTALL);
        Matcher matcher = codePattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String code = matcher.group(1);
            String highlightedCode = highlightJavaCode(code);
            matcher.appendReplacement(sb, "<pre><code>" + highlightedCode + "</code></pre>");
        }
        matcher.appendTail(sb);
        html = sb.toString();

        return "<html><head><style>"
                + getCodeStyle()
                + "</style></head><body>"
                + html
                + "</body></html>";
    }

    private void updateDryRunPrompt(String prompt) {
        lastMarkdown = prompt;
        String htmlContent = convertMarkdownToHtml(prompt);
        editorPane.setText(htmlContent);
        editorPane.setCaretPosition(0); // Scroll to top
    }
}
