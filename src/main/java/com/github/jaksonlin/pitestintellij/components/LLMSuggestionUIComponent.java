package com.github.jaksonlin.pitestintellij.components;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.github.jaksonlin.pitestintellij.viewmodels.LLMSuggestionUIComponentViewModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.github.jaksonlin.pitestintellij.services.LLMService;

public class LLMSuggestionUIComponent implements BasicEventObserver {
    private final LLMSuggestionUIComponentViewModel viewModel;
    private final ChatPanel chatPanel = new ChatPanel();
    private final LLMResponsePanel responsePanel = new LLMResponsePanel(chatPanel);
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final DefaultComboBoxModel<FileItem> fileListModel = new DefaultComboBoxModel<>();
    private final JComboBox<FileItem> fileSelector = new ComboBox<>(fileListModel);
    private final JButton generateButton = new JButton("Generate Suggestions");
    private final JButton dryRunButton = new JButton("Dry Run");
    private List<FileItem> allFileItems = new ArrayList<>();

    public LLMSuggestionUIComponent(LLMService llmService) {
        setupUI();
        // setup message routing
        viewModel = new LLMSuggestionUIComponentViewModel(llmService);
        viewModel.addObserver(this);
        viewModel.addObserver(responsePanel);

        // add the chatPanel to the mainPanel
        chatPanel.addListener(message -> {
            viewModel.handleChatMessage(message);
        });

        // add the reponse listener to the responsePanel
        responsePanel.addResponseActionListener(new LLMResponsePanel.ResponseActionListener() {
            @Override
            public void onClearButtonClick() {
                viewModel.clearChat();
            }

            @Override
            public void onCopyButtonClick() {
                viewModel.copyChat();
            }
        });
        
    }


    
    @Override
    public void onEventHappen(String eventName, Object eventObj) {
        switch (eventName) {
            case "START_LOADING":
                generateButton.setEnabled(false);
                dryRunButton.setEnabled(false);
                break;
            case "STOP_LOADING":
                generateButton.setEnabled(true);
                dryRunButton.setEnabled(true);
                break;
            case "RUN_HISTORY_LIST":
                ApplicationManager.getApplication().invokeLater(() -> loadFileHistory(eventObj));
                break;
            default:
                break;
        }
    }

    public JPanel getPanel() {
        return this.mainPanel;
    }

    private void setupUI() {
        // Create the top panel for file selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(JBUI.Borders.empty(10));

        // Setup file selector with search capability
        fileSelector.setPreferredSize(new Dimension(400, 30));
        fileSelector.setMaximumRowCount(15);
        fileSelector.setEditable(false);
        
        // Create and set custom editor
        JTextField editorComponent = new JTextField();
        ComboBoxEditor editor = new BasicComboBoxEditor() {
            @Override
            public Component getEditorComponent() {
                return editorComponent;
            }
        };
        fileSelector.setEditor(editor);

        fileSelector.addActionListener(e -> {
            
                onFileSelected();
            
        });

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(dryRunButton);
        buttonPanel.add(generateButton);

        // Setup action listeners
        generateButton.addActionListener(e -> {
            FileItem selectedItem = (FileItem) fileSelector.getSelectedItem();
            if (selectedItem != null) {
                viewModel.generateSuggestions(selectedItem.context);
            }
        });

        dryRunButton.addActionListener(e -> {
            FileItem selectedItem = (FileItem) fileSelector.getSelectedItem();
            if (selectedItem != null) {
                viewModel.dryRunGetPrompt(selectedItem.context);
            }
        });

        // Add components to top panel
        JPanel selectorPanel = new JPanel(new BorderLayout());
        selectorPanel.add(new JLabel("Select File: "), BorderLayout.WEST);
        selectorPanel.add(fileSelector, BorderLayout.CENTER);
        topPanel.add(selectorPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Add panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(responsePanel, BorderLayout.CENTER);
    }



    private void loadFileHistory(Object eventObj) {
        if (eventObj == null) {
            fileListModel.removeAllElements();
            allFileItems.clear();
            return;
        }
        if (!(eventObj instanceof Map<?, ?>)) {
            return;
        }
        
        Map<String, PitestContext> history = (Map<String, PitestContext>) eventObj;
        if (history.isEmpty()) {
            return;
        }
        fileListModel.removeAllElements();
        allFileItems.clear();

        history.forEach((key, context) -> {
            String displayName = String.format("%s.%s",
                    context.getTargetClassPackageName(),
                    context.getTargetClassName());
            String filePath = context.getTargetClassFilePath();
            allFileItems.add(new FileItem(displayName, filePath, context));
        });

        // Sort items by display name
        allFileItems.sort((a, b) -> a.displayName.compareTo(b.displayName));

        // If there was a previously selected item, try to maintain the selection
        FileItem selectedItem = (FileItem) fileSelector.getSelectedItem();
        String previousSelection = selectedItem != null ? selectedItem.filePath : null;

        // Add all items to the model
        allFileItems.forEach(fileListModel::addElement);

        // Restore previous selection if possible
        if (previousSelection != null) {
            for (int i = 0; i < fileListModel.getSize(); i++) {
                FileItem item = fileListModel.getElementAt(i);
                if (item.filePath.equals(previousSelection)) {
                    fileSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void onFileSelected() {
        // This method can be used to perform any actions when a file is selected
        // For now, we'll leave it empty as generation is handled by the button
    }


    // Helper class to store file information in the combo box
    private static class FileItem {
        final String displayName;
        final String filePath;
        final PitestContext context;

        FileItem(String displayName, String filePath, PitestContext context) {
            this.displayName = displayName;
            this.filePath = filePath;
            this.context = context;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
} 