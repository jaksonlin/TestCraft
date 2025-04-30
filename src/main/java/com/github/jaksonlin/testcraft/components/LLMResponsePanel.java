package com.github.jaksonlin.testcraft.components;

import com.github.jaksonlin.testcraft.observers.BasicEventObserver;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import com.intellij.ui.JBColor;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import com.intellij.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.text.html.HTMLDocument;

public class LLMResponsePanel extends JPanel implements BasicEventObserver {
    private static final String BUNDLE = "messages.MyBundle";
    private static ResourceBundle ourBundle;
    
    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return AbstractBundle.message(getBundle(), key, params);
    }
    
    private static ResourceBundle getBundle() {
        if (ourBundle == null) {
            ourBundle = ResourceBundle.getBundle(BUNDLE);
        }
        return ourBundle;
    }

    private final JEditorPane outputArea;
    private final ChatPanel chatPanel;
    private boolean isLoading = false;
    private boolean copyAsMarkdown = false;
    private final StringBuilder chatHistory = new StringBuilder();
    private final HTMLEditorKit htmlKit;
    private final StyleSheet styleSheet;
    private final JPanel loadingPanel;
    private final Timer loadingTimer;
    private final JLabel loadingLabel;

    private static final String BASE_HTML_TEMPLATE =
            "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <style>\n" +
                    "        %s\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div id=\"chat-container\">\n" +
                    "        %s\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

    private static final String MESSAGE_TEMPLATE =
            "<div class=\"message %s\">\n" +
                    "    <div class=\"message-header\">%s</div>\n" +
                    "    <div class=\"message-content\">%s</div>\n" +
                    "</div>\n" +
                    "<div class=\"message-separator\"></div>";

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

    public void notifyCopyButtonClick() {
        for (ResponseActionListener listener : responseActionListeners) {
            listener.onCopyButtonClick();
        }
    }

    public LLMResponsePanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Setup improved JEditorPane for HTML rendering
        outputArea = new JEditorPane();
        outputArea.setEditable(false);
        outputArea.setContentType("text/html");
        
        // Configure HTML editor kit with custom style sheet
        htmlKit = new HTMLEditorKit();
        styleSheet = htmlKit.getStyleSheet();
        styleSheet.addRule(getCodeStyle());
        outputArea.setEditorKit(htmlKit);
        
        // Initialize with empty document
        HTMLDocument doc = (HTMLDocument) htmlKit.createDefaultDocument();
        outputArea.setDocument(doc);
        
        // Enable proper HTML rendering
        outputArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        outputArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        
        // Create loading panel
        loadingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loadingPanel.setVisible(false);
        loadingLabel = new JLabel(message("llm.thinking"));
        loadingPanel.add(loadingLabel);
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        loadingPanel.setBackground(JBColor.background());
        
        // Setup loading animation timer
        loadingTimer = new Timer();
        
        JBScrollPane outputScrollPane = new JBScrollPane(outputArea);
        
        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(JBUI.Borders.empty(2, 2));

        JButton copyButton = new JButton(message("llm.copy.to.clipboard"));
        copyButton.addActionListener(e -> {
            notifyCopyButtonClick();
        });
        toolbar.add(copyButton);

        JButton clearButton = new JButton(message("llm.clear"));
        clearButton.addActionListener(e -> {
            clearOutput();
            notifyClearButtonClick();
        });
        toolbar.add(clearButton);
        
        // Create input panel at the bottom
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatPanel.getInputPanel(), BorderLayout.CENTER);
        
        // Create center panel to hold toolbar, output area, and loading panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(outputScrollPane, BorderLayout.CENTER);
        centerPanel.add(loadingPanel, BorderLayout.SOUTH);
        
        // Add components
        add(centerPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // add the chatPanel to the responsePanel, and notify update on the responsePanel with user message
        this.chatPanel.addListener(message -> {
            onEventHappen("CHAT_REQUEST", message);
        });
        
        // Initialize with empty chat container
        updateOutputArea();
    }

    private String getCodeStyle() {
        boolean isDarkTheme = !JBColor.isBright();
        String backgroundColor = isDarkTheme ? "#2b2d30" : "#fafafa";
        String textColor = isDarkTheme ? "#bababa" : "#2b2b2b";
        String codeBackground = isDarkTheme ? "#1e1f22" : "#f6f8fa";
        String codeBorder = isDarkTheme ? "#1e1f22" : "#e1e4e8";
        String linkColor = isDarkTheme ? "#589df6" : "#2470B3";
        String separatorColor = isDarkTheme ? "#3c3f41" : "#e0e0e0";

        return "body {\n" +
                "    font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif;\n" +
                "    font-size: 13pt;\n" +
                "    margin: 10px;\n" +
                "    line-height: 1.4;\n" +
                "    background-color: " + backgroundColor + ";\n" +
                "    color: " + textColor + ";\n" +
                "}\n" +
                "#chat-container {\n" +
                "    display: flex;\n" +
                "    flex-direction: column;\n" +
                "    gap: 0.5em;\n" +
                "}\n" +
                ".message {\n" +
                "    border-radius: 8px;\n" +
                "    padding: 12px;\n" +
                "}\n" +
                ".message.user {\n" +
                "    background-color: " + (isDarkTheme ? "#2d2d2d" : "#e3f2fd") + ";\n" +
                "    margin-left: 20%;\n" +
                "}\n" +
                ".message.assistant {\n" +
                "    background-color: " + (isDarkTheme ? "#1e1e1e" : "#f5f5f5") + ";\n" +
                "    margin-right: 20%;\n" +
                "}\n" +
                ".message.system {\n" +
                "    background-color: " + (isDarkTheme ? "#2d2d2d" : "#e8f5e9") + ";\n" +
                "    text-align: center;\n" +
                "    font-style: italic;\n" +
                "}\n" +
                ".message-header {\n" +
                "    font-weight: bold;\n" +
                "    margin-bottom: 4px;\n" +
                "    color: " + textColor + ";\n" +
                "}\n" +
                ".message-content {\n" +
                "    white-space: pre-wrap;\n" +
                "}\n" +
                ".message-separator {\n" +
                "    height: 1px;\n" +
                "    background-color: " + separatorColor + ";\n" +
                "    margin: 10px 0;\n" +
                "}\n" +
                "pre {\n" +
                "    background-color: " + codeBackground + ";\n" +
                "    padding: 16px;\n" +
                "    border-radius: 6px;\n" +
                "    margin: 1em 0;\n" +
                "    border: 1px solid " + codeBorder + ";\n" +
                "    overflow: auto;\n" +
                "}\n" +
                "code {\n" +
                "    font-family: \"JetBrains Mono\", \"Fira Code\", Consolas, Monaco, \"Courier New\", monospace;\n" +
                "    font-size: 12pt;\n" +
                "    color: " + textColor + ";\n" +
                "}\n" +
                "a { color: " + linkColor + "; }\n";
    }

    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private void startLoading() {
        isLoading = true;
        loadingPanel.setVisible(true);
        
        // Start the loading animation
        loadingTimer.scheduleAtFixedRate(new TimerTask() {
            private int dots = 0;
            @Override
            public void run() {
                if (!isLoading) {
                    cancel();
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    dots = (dots + 1) % 4;
                    loadingLabel.setText(message("llm.thinking") + repeatString(".", dots));
                });
            }
        }, 0, 500);
        
        // Disable input while loading
        chatPanel.setInputEnabled(false);
    }

    private void stopLoading() {
        if (!isLoading) {
            return; // Prevent multiple stop calls
        }
        isLoading = false;
        loadingPanel.setVisible(false);
        loadingTimer.purge();
        
        // Re-enable input after loading
        chatPanel.setInputEnabled(true);
    }

    private void updateOutputArea() {
        String fullHtml = String.format(BASE_HTML_TEMPLATE, getCodeStyle(), chatHistory.toString());
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a new document each time to avoid state issues
                HTMLDocument doc = (HTMLDocument) htmlKit.createDefaultDocument();
                // Set the document first
                outputArea.setDocument(doc);
                // Then insert the content
                htmlKit.insertHTML(doc, 0, fullHtml, 0, 0, null);
                outputArea.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                // Fallback to setText if something goes wrong
                outputArea.setText(fullHtml);
            }
        });
    }

    private void appendMarkdownToOutput(String markdown) {
        String htmlContent = convertMarkdownToHtml(markdown);
        chatHistory.append(htmlContent);
        updateOutputArea();
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private void clearOutput() {
        chatHistory.setLength(0);
        updateOutputArea();
    }

    private void copyToClipboard(Object eventObj) {
        String currentContentToCopy;

        if (copyAsMarkdown) {
            currentContentToCopy = eventObj.toString();
        } else {
            currentContentToCopy = outputArea.getText();
        }
        StringSelection selection = new StringSelection(currentContentToCopy);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        switch (eventName) {
            case "START_LOADING":
                startLoading();
                break;
            case "STOP_LOADING":
                stopLoading();
                break;
            case "COPY_CHAT_RESPONSE":
                copyToClipboard(eventObj);
                break;
            case "CONFIG_CHANGE:copyAsMarkdown":
                copyAsMarkdown = (boolean) eventObj;
                break;
            case "CHAT_REQUEST":
                if (!isLoading) {
                    appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "user", message("llm.user"), eventObj.toString()));
                    startLoading();
                }
                break;
            case "DRY_RUN_PROMPT":
                String dryRunPrompt = (String) eventObj;
                if (dryRunPrompt.isEmpty()){
                    JOptionPane.showMessageDialog(this, message("llm.dry.run.prompt.empty"), message("llm.dry.run.prompt"), JOptionPane.INFORMATION_MESSAGE);
                } else {
                    appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "system", message("llm.system"), message("llm.dry.run.prompt") + "\n" + eventObj.toString()));
                }
                break;
            default:
                String[] responseType = eventName.split(":");
                if (responseType.length > 1 && responseType[0].equals("CHAT_RESPONSE")) {
                    switch (responseType[1]) {
                        case "CHAT_MESSAGE":
                            appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "assistant", message("llm.assistant"), eventObj.toString()));
                            break;
                        case "ERROR":
                            JOptionPane.showMessageDialog(this, message("llm.error") + ": " + eventObj.toString(), message("llm.error"), JOptionPane.ERROR_MESSAGE);
                            break;
                        case "UNIT_TEST_REQUEST":
                            clearOutput();
                            appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "system", message("llm.system"), message("llm.new.unit.test.suggestion") + "\n" + eventObj.toString()));
                            break;
                    }
                }
                break;
        }
    }
}
