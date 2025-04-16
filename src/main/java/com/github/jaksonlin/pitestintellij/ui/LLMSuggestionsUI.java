package com.github.jaksonlin.pitestintellij.ui;

import com.github.jaksonlin.pitestintellij.components.LLMResponsePanel;
import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.observers.BasicEventObserver;
import com.github.jaksonlin.pitestintellij.services.RunHistoryManager;
import com.github.jaksonlin.pitestintellij.viewmodels.LLMSuggestionsPanelViewModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.github.jaksonlin.pitestintellij.util.Pair;


public class LLMSuggestionsUI implements BasicEventObserver {
    private final Project project;
    private final LLMSuggestionsPanelViewModel vm;
    private final LLMResponsePanel responsePanel = new LLMResponsePanel();
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final DefaultComboBoxModel<FileItem> fileListModel = new DefaultComboBoxModel<>();
    private final JComboBox<FileItem> fileSelector = new ComboBox<>(fileListModel);
    private final RunHistoryManager historyManager;

    public LLMSuggestionsUI(Project project) {
        this.project = project;
        this.historyManager = project.getService(RunHistoryManager.class);
        this.vm = new LLMSuggestionsPanelViewModel(project, responsePanel);
        setupUI();
        loadFileHistory();
        historyManager.addObserver(this);
    }

    private void setupUI() {
        // Create the top panel for file selection
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(JBUI.Borders.empty(5));

        // Setup file selector
        fileSelector.setPreferredSize(new Dimension(400, 30));
        fileSelector.addActionListener(e -> onFileSelected());

        // Create generate button
        JButton generateButton = new JButton("Generate Suggestions");
        generateButton.addActionListener(e -> onGenerateClicked());

        // Add components to top panel
        topPanel.add(new JLabel("Select File: "), BorderLayout.WEST);
        topPanel.add(fileSelector, BorderLayout.CENTER);
        topPanel.add(generateButton, BorderLayout.EAST);

        // Add panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(responsePanel, BorderLayout.CENTER);
    }

    @Override
    public void onEventHappen(Object event) {
        if (event instanceof List<?>) {
            List<?> list = (List<?>) event;
            if (!list.isEmpty() && list.get(0) instanceof Pair) {
                // We're receiving a List<Pair<String, String>> from RunHistoryManager
                ApplicationManager.getApplication().invokeLater(this::loadFileHistory);
            }
        }
    }

    private void loadFileHistory() {
        fileListModel.removeAllElements();
        Map<String, PitestContext> history = historyManager.getRunHistory();
        
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

    private void onGenerateClicked() {
        FileItem selectedItem = (FileItem) fileSelector.getSelectedItem();
        if (selectedItem != null) {
            vm.generateSuggestions(selectedItem.context);
        }
    }

    public JPanel getPanel() {
        return this.mainPanel;
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