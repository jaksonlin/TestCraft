package com.github.jaksonlin.testcraft.presentation.components.llmchat;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ChatEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.TypedEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

public class ChatPanelComponent {

    private final TypedEventObserver<ChatEvent> chatObserver = new TypedEventObserver<ChatEvent>(ChatEvent.class) {
        @Override
        protected void onTypedEvent(ChatEvent event) {
            // Handle the chat event
            switch (event.getEventType()) {
                case ChatEvent.START_LOADING:
                    // disable input area and send button
                    setInputEnabled(false);
                    break;
                case ChatEvent.STOP_LOADING:
                case ChatEvent.CHAT_RESPONSE:
                case ChatEvent.ERROR:
                    // enable input area and send button
                    setInputEnabled(true);
                    break;
            }
        }
    };
    private final JTextArea inputArea;
    private final JButton sendButton;
    private final JPanel masterPanel;
    
    public ChatPanelComponent() {

        // Input area
        inputArea = new JTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JBScrollPane inputScrollPane = new JBScrollPane(inputArea);

        // Send button
        sendButton = new JButton(I18nService.getInstance().message("chat.send.button"));
        sendButton.addActionListener(e -> sendMessage());

        // Input panel (input area + send button)
        masterPanel = new JPanel(new BorderLayout());
        masterPanel.setBorder(JBUI.Borders.empty(5));
        masterPanel.add(inputScrollPane, BorderLayout.CENTER);
        masterPanel.add(sendButton, BorderLayout.EAST);

        // Add key listener for Ctrl+Enter to send
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendMessage();
                    e.consume();
                }
            }
        });


    }


    public JPanel getInputPanel() {
        return masterPanel;
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            EventBusService.getInstance().post(new ChatEvent(ChatEvent.CHAT_REQUEST, message));
            inputArea.setText("");
            inputArea.requestFocus();
        }
    }

    public void clear() {
        inputArea.setText("");
    }

    public void setInputEnabled(boolean enabled) {
        inputArea.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        if (enabled) {
            inputArea.requestFocus();
        }
    }

    public void dispose() {
        chatObserver.unregister();
    }
} 