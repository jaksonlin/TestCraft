package com.github.jaksonlin.pitestintellij.components;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.github.jaksonlin.pitestintellij.viewmodels.LLMSuggestionUIComponentViewModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.github.jaksonlin.pitestintellij.services.LLMService;

public class LLMSuggestionUIComponent implements BasicEventObserver {
    private final LLMSuggestionUIComponentViewModel viewModel;
    private final LLMResponsePanel responsePanel = new LLMResponsePanel();
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final DefaultComboBoxModel<FileItem> fileListModel = new DefaultComboBoxModel<>();
    private final JComboBox<FileItem> fileSelector = new ComboBox<>(fileListModel);
    private final JButton generateButton = new JButton("Generate Suggestions");
    private final JButton dryRunButton = new JButton("Dry Run");

    public LLMSuggestionUIComponent(LLMService llmService) {
        setupUI();
        llmService.addObserver(responsePanel);
        llmService.addObserver(this);
        viewModel = new LLMSuggestionUIComponentViewModel(llmService);
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
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(JBUI.Borders.empty(5));

        // Setup file selector
        fileSelector.setPreferredSize(new Dimension(400, 30));
        fileSelector.addActionListener(e -> onFileSelected());

        // Create button panel for side-by-side buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(dryRunButton);
        buttonPanel.add(generateButton);

        // Create generate button
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
        topPanel.add(new JLabel("Select File: "), BorderLayout.WEST);
        topPanel.add(fileSelector, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Add panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(responsePanel, BorderLayout.CENTER);
    }


    private void loadFileHistory(Object eventObj) {

        if (eventObj == null) {
            // clear the file selector
            fileListModel.removeAllElements();
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

        List<FileItem> items = new ArrayList<>();
        history.forEach((key, context) -> {
            String displayName = String.format("%s.%s",
                    context.getTargetClassPackageName(),
                    context.getTargetClassName());
            String filePath = context.getTargetClassFilePath();
            items.add(new FileItem(displayName, filePath, context));
        });

        // Sort items by display name
        items.sort((a, b) -> a.displayName.compareTo(b.displayName));

        // If there was a previously selected item, try to maintain the selection
        FileItem selectedItem = (FileItem) fileSelector.getSelectedItem();
        String previousSelection = selectedItem != null ? selectedItem.filePath : null;

        items.forEach(fileListModel::addElement);

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