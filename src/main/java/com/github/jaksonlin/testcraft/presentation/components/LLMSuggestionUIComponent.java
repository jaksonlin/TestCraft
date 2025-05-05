package com.github.jaksonlin.testcraft.presentation.components;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.RunHistoryEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ChatEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.TypedEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.github.jaksonlin.testcraft.presentation.viewmodels.LLMSuggestionUIComponentViewModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ComboBoxEditor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LLMSuggestionUIComponent  {

    private final TypedEventObserver<ChatEvent> chatEventObserver;
    private final TypedEventObserver<RunHistoryEvent> runHistoryEventObserver;

    
    private final ChatPanelComponent chatPanel = new ChatPanelComponent();
    private final LLMResponseComponent responsePanel = new LLMResponseComponent(chatPanel);
    
    private final LLMSuggestionUIComponentViewModel viewModel;

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final DefaultComboBoxModel<FileItem> fileListModel = new DefaultComboBoxModel<>();
    private final JComboBox<FileItem> fileSelector = new ComboBox<>(fileListModel);
    private final JButton generateButton = new JButton(I18nService.getInstance().message("llm.generate.suggestions"));
    private final JButton dryRunButton = new JButton(I18nService.getInstance().message("llm.check.prompt"));
    private List<FileItem> allFileItems = new ArrayList<>();

    public LLMSuggestionUIComponent() {
        setupUI();
        
        chatEventObserver = new TypedEventObserver<ChatEvent>(ChatEvent.class) {
            @Override
            public void onTypedEvent(ChatEvent event) {
                switch (event.getEventType()) {
                    case ChatEvent.START_LOADING:
                        generateButton.setEnabled(false);
                        dryRunButton.setEnabled(false);
                        break;
                    case ChatEvent.STOP_LOADING:
                        generateButton.setEnabled(true);
                        dryRunButton.setEnabled(true);
                        break;
                }
            }
        };
        runHistoryEventObserver = new TypedEventObserver<RunHistoryEvent>(RunHistoryEvent.class) {
            @Override
            public void onTypedEvent(RunHistoryEvent event) {
                switch (event.getEventType()) {
                    case RunHistoryEvent.RUN_HISTORY_LIST:
                        ApplicationManager.getApplication().invokeLater(() -> loadFileHistory(event.getPayload()));
                        break;
                }
            }
        };
        // setup message routing
        viewModel = new LLMSuggestionUIComponentViewModel();
        // propagate the config change
        viewModel.propagateConfigChange();
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
        selectorPanel.add(new JLabel(I18nService.getInstance().message("llm.select.file") + ": "), BorderLayout.WEST);
        selectorPanel.add(fileSelector, BorderLayout.CENTER);
        topPanel.add(selectorPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Add panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(responsePanel.getMasterPanel(), BorderLayout.CENTER);
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