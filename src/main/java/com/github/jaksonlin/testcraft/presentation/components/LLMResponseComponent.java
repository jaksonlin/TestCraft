package com.github.jaksonlin.testcraft.presentation.components;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ChatEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.LLMConfigEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.TypedEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
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
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.text.html.HTMLDocument;

public class LLMResponseComponent  {
    private final TypedEventObserver<ChatEvent> eventObserver = new TypedEventObserver<ChatEvent>(ChatEvent.class) {
        @Override
        public void onTypedEvent(ChatEvent event) {
            switch (event.getEventType()) {
                case ChatEvent.START_LOADING:
                    startLoading();
                    break;
                case ChatEvent.STOP_LOADING:
                    stopLoading();
                    break;
                case ChatEvent.CHAT_REQUEST:
                    if (!isLoading) {
                        appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "user", I18nService.getInstance().message("llm.user"), event.getPayload().toString()));
                        startLoading();
                    }
                    break;
                case ChatEvent.CHAT_RESPONSE:
                    if (isLoading) {
                        stopLoading();
                    }
                    appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "assistant", I18nService.getInstance().message("llm.assistant"), event.getPayload().toString()));
                    break;
                case ChatEvent.COPY_CHAT_RESPONSE:
                    copyToClipboard(event.getPayload());
                    break;
                case ChatEvent.DRY_RUN_PROMPT:
                    String dryRunPrompt = (String) event.getPayload();
                    if (dryRunPrompt.isEmpty()){
                        JOptionPane.showMessageDialog(masterPanel, I18nService.getInstance().message("llm.dry.run.prompt.empty"), I18nService.getInstance().message("llm.dry.run.prompt"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        appendMarkdownToOutput(String.format(MESSAGE_TEMPLATE, "system", I18nService.getInstance().message("llm.system"), I18nService.getInstance().message("llm.dry.run.prompt") + "\n" + event.getPayload().toString()));
                    }
                    break;
                case ChatEvent.ERROR:
                    JOptionPane.showMessageDialog(masterPanel, I18nService.getInstance().message("llm.error") + ": " + event.getPayload().toString(), I18nService.getInstance().message("llm.error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
        }
    };

    private final TypedEventObserver<LLMConfigEvent> llmConfigObserver = new TypedEventObserver<LLMConfigEvent>(LLMConfigEvent.class) {
        @Override
        public void onTypedEvent(LLMConfigEvent event) {
            switch (event.getEventType()) {
                case LLMConfigEvent.CONFIG_CHANGE_COPY_AS_MARKDOWN:
                    copyAsMarkdown = (boolean) event.getPayload();
                    break;
            }
        }
    };
    private final JEditorPane outputArea;
    private boolean isLoading = false;
    private boolean copyAsMarkdown = false;
   
    private final HTMLEditorKit htmlKit;
    private final StyleSheet styleSheet;
    private final JPanel masterPanel;
    private final JPanel loadingPanel;
    private final Timer loadingTimer;
    private final JLabel loadingLabel;
    

    private final StringBuilder chatHistory = new StringBuilder();

    public JPanel getMasterPanel() {
        return masterPanel;
    }
    

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

    
    public void notifyClearButtonClick() {
        EventBusService.getInstance().post(new ChatEvent(ChatEvent.CLEAR_CHAT, null));
    }

    public void notifyCopyButtonClick() {
        EventBusService.getInstance().post(new ChatEvent(ChatEvent.REQUEST_COPY_CHAT_RESPONSE, null));
    }

    public LLMResponseComponent(ChatPanelComponent chatPanelComponent) {
        masterPanel = new JPanel(new BorderLayout());
        masterPanel.setBorder(JBUI.Borders.empty(10));


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
        loadingLabel = new JLabel(I18nService.getInstance().message("llm.thinking"));
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

        JButton copyButton = new JButton(I18nService.getInstance().message("llm.copy.to.clipboard"));
        copyButton.addActionListener(e -> {
            notifyCopyButtonClick();
        });
        toolbar.add(copyButton);

        JButton clearButton = new JButton(I18nService.getInstance().message("llm.clear"));
        clearButton.addActionListener(e -> {
            clearOutput();
            notifyClearButtonClick();
        });
        toolbar.add(clearButton);
        
        // Create input panel at the bottom
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatPanelComponent.getInputPanel(), BorderLayout.CENTER);
        
        // Create center panel to hold toolbar, output area, and loading panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(outputScrollPane, BorderLayout.CENTER);
        centerPanel.add(loadingPanel, BorderLayout.SOUTH);
        
        // Add components
        masterPanel.add(centerPanel, BorderLayout.CENTER);
        masterPanel.add(inputPanel, BorderLayout.SOUTH);

        
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

        return "body { " +
                "font-family: Arial, sans-serif; " +
                "font-size: 13pt; " +
                "margin: 10px; " +
                "background-color: " + backgroundColor + "; " +
                "color: " + textColor + "; " +
                "}\n" +
                ".message { " +
                "padding: 12px; " +
                "margin: 5px 0; " +
                "}\n" +
                ".message.user { " +
                "background-color: " + (isDarkTheme ? "#2d2d2d" : "#e3f2fd") + "; " +
                "margin-left: 20%; " +
                "}\n" +
                ".message.assistant { " +
                "background-color: " + (isDarkTheme ? "#1e1e1e" : "#f5f5f5") + "; " +
                "margin-right: 20%; " +
                "}\n" +
                ".message.system { " +
                "background-color: " + (isDarkTheme ? "#2d2d2d" : "#e8f5e9") + "; " +
                "text-align: center; " +
                "font-style: italic; " +
                "}\n" +
                ".message-header { " +
                "font-weight: bold; " +
                "margin-bottom: 4px; " +
                "}\n" +
                ".message-content { " +
                "white-space: pre-wrap; " +
                "}\n" +
                ".message-separator { " +
                "border-top: 1px solid " + separatorColor + "; " +
                "margin: 10px 0; " +
                "}\n" +
                "pre { " +
                "background-color: " + codeBackground + "; " +
                "padding: 16px; " +
                "margin: 1em 0; " +
                "border: 1px solid " + codeBorder + "; " +
                "}\n" +
                "code { " +
                "font-family: monospace; " +
                "font-size: 12pt; " +
                "}\n" +
                "a { color: " + linkColor + "; }";
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
                    loadingLabel.setText(I18nService.getInstance().message("llm.thinking") + repeatString(".", dots));
                });
            }
        }, 0, 500);
        
    }

    private void stopLoading() {
        if (!isLoading) {
            return; // Prevent multiple stop calls
        }
        isLoading = false;
        loadingPanel.setVisible(false);
        loadingTimer.purge();
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
}
