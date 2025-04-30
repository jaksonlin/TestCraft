package com.github.jaksonlin.testcraft.presentation.components;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChatPanel extends JPanel {
    private final JTextArea inputArea;
    private final JButton sendButton;
    private final List<ChatMessageListener> listeners = new ArrayList<>();
    private final JPanel inputPanel;

    public interface ChatMessageListener {
        void onNewMessage(String message);
    }

    public ChatPanel() {
        setLayout(new BorderLayout());

        // Input area
        inputArea = new JTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JBScrollPane inputScrollPane = new JBScrollPane(inputArea);

        // Send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        // Input panel (input area + send button)
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(JBUI.Borders.empty(5));
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

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

    public void addListener(ChatMessageListener listener) {
        listeners.add(listener);
    }

    public JPanel getInputPanel() {
        return inputPanel;
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            listeners.forEach(listener -> listener.onNewMessage(message));
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
} 