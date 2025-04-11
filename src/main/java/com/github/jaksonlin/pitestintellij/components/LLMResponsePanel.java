package com.github.jaksonlin.pitestintellij.components;

import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import com.intellij.ui.JBColor;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

public class LLMResponsePanel extends JPanel implements BasicEventObserver {
    private final JEditorPane outputArea;
    private final ChatPanel chatPanel;
    private Timer loadingTimer;
    private int loadingDots = 0;
    private boolean isLoading = false;
    private boolean copyAsMarkdown = false;
    private final StringBuilder chatHistory = new StringBuilder();
    private static final String BASE_HTML_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                %s
            </style>
        </head>
        <body>
            <div id="chat-container">
                %s
            </div>
        </body>
        </html>
        """;

    private static final String MESSAGE_TEMPLATE = """
        <div class="message %s">
            <div class="message-header">%s</div>
            <div class="message-content">%s</div>
        </div>
        """;

    public interface ResponseActionListener {
        void onClearButtonClick();
        void onCopyButtonClick();
    }

    private List<ResponseActionListener> responseActionListeners = new ArrayList<>();

    public void addResponseActionListener(ResponseActionListener listener) {
        responseActionListeners.add(listener);
    }

    public void removeResponseActionListener(ResponseActionListener listener) {
        responseActionListeners.remove(listener);
    }

    public void notifyClearButtonClick() {
        
        for (ResponseActionListener listener : responseActionListeners) {
            listener.onClearButtonClick();
        }
    }

    private void clearOutput() {
        chatHistory.setLength(0);
        updateOutputArea();
    }

    public void notifyCopyButtonClick() {
        for (ResponseActionListener listener : responseActionListeners) {
            listener.onCopyButtonClick();
        }
    }

    public LLMResponsePanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
        // add the chatPanel to the responsePanel, and notify update on the responsePanel with user message
        this.chatPanel.addListener(message -> {
            onEventHappen("CHAT_REQUEST", message);
        });

        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Setup improved JEditorPane for HTML rendering
        outputArea = new JEditorPane();
        outputArea.setEditable(false);
        outputArea.setContentType("text/html");
        
        // Configure HTML editor kit with custom style sheet
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(getCodeStyle());
        outputArea.setEditorKit(kit);
        
        // Enable proper HTML rendering
        outputArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        outputArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        JBScrollPane outputScrollPane = new JBScrollPane(outputArea);
        
        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(JBUI.Borders.empty(2, 2));

        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(e -> {
            notifyCopyButtonClick();
        }
        );
        toolbar.add(copyButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            clearOutput();
            notifyClearButtonClick();
        });
        toolbar.add(clearButton);
        
        // Create input panel at the bottom
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatPanel.getInputPanel(), BorderLayout.CENTER);
        
        // Create center panel to hold toolbar and output area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(outputScrollPane, BorderLayout.CENTER);
        
        // Add components
        add(centerPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);


    }

    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        switch (eventName) {
            case "COPY_CHAT_RESPONSE":
                copyToClipboard(eventObj.toString());
                break;
            case "CONFIG_CHANGE:copyAsMarkdown":
                copyAsMarkdown = (boolean) eventObj;
                break;
            case "CHAT_REQUEST":
                appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "user", "User", eventObj.toString()));
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
                String[] responseType = eventName.split(":");
                if (responseType.length > 1 && responseType[0].equals("CHAT_RESPONSE")) {
                    switch (responseType[1]) {
                        case "CHAT_MESSAGE":
                            appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "assistant", "Assistant", eventObj.toString()));
                            break;
                        case "ERROR":
                            JOptionPane.showMessageDialog(this, "Error: " + eventObj.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                            break;
                        case "UNIT_TEST_REQUEST":
                            clearOutput();
                            appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "system", "System", "New Unit Test Suggestion\n" + eventObj.toString()));
                            break;
                    }
                }
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
            #chat-container {
                display: flex;
                flex-direction: column;
                gap: 1em;
            }
            .message {
                border-radius: 8px;
                padding: 12px;
                margin-bottom: 8px;
            }
            .message.user {
                background-color: %s;
                margin-left: 20%%;
            }
            .message.assistant {
                background-color: %s;
                margin-right: 20%%;
            }
            .message.system {
                background-color: %s;
                text-align: center;
                font-style: italic;
            }
            .message-header {
                font-weight: bold;
                margin-bottom: 4px;
                color: %s;
            }
            .message-content {
                white-space: pre-wrap;
            }
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
            backgroundColor, textColor,
            isDarkTheme ? "#2d2d2d" : "#e3f2fd",  // user message background
            isDarkTheme ? "#1e1e1e" : "#f5f5f5",  // assistant message background
            isDarkTheme ? "#2d2d2d" : "#e8f5e9",  // system message background
            textColor,
            codeBackground, codeBorder, textColor, linkColor,
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

    private void appendMarkdownToOutput(String markdown) {
        String htmlContent = convertMarkdownToHtml(markdown);
        chatHistory.append(htmlContent);
        updateOutputArea();
    }

    private void updateOutputArea() {
        String fullHtml = String.format(BASE_HTML_TEMPLATE, getCodeStyle(), chatHistory.toString());
        outputArea.setText(fullHtml);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
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
                String loadingText = String.format(MESSAGE_TEMPLATE, "system", "System", "Generating suggestions" + dots);
                
                String fullHtml = String.format(BASE_HTML_TEMPLATE, getCodeStyle(), chatHistory + loadingText);
                outputArea.setText(fullHtml);
            }
        }, 0, 500);
    }

    private void stopLoading() {
        isLoading = false;
        if (loadingTimer != null) {
            loadingTimer.cancel();
            loadingTimer = null;
        }
    }

    private void copyToClipboard(String contentToCopy) {
        String currentContentToCopy;
        if (contentToCopy == null || contentToCopy.isEmpty()) {
            // which means the llmservice mediator is empty, any copy operation can only happen on our outputArea
            currentContentToCopy = outputArea.getText();
        } else {
            // what ever return from the llmservice mediator, we will copy it to the clipboard, this is what rendered in the outputArea
            currentContentToCopy = contentToCopy;
        }
        StringSelection selection = new StringSelection(currentContentToCopy);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private void updateDryRunPrompt(String prompt) {
        clearOutput();
        appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "system", "System", "Dry Run Prompt\n" + prompt));
    }
}
