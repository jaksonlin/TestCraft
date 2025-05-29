package com.github.jaksonlin.testcraft.presentation.components.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemSearchAdditionComponent<T> {
    private final Project project;
    private final JPanel mainPanel;
    private final JTextField searchField;
    private final JList<T> searchResultsList;
    private final JList<T> candidateList;
    private final DefaultListModel<T> searchResultsModel;
    private final DefaultListModel<T> candidateModel;
    private final JButton addButton;
    private final JButton removeButton;
    private final JButton clearButton;
    private final JButton confirmButton;
    private final Function<String, List<T>> searchFunction;
    private final Function<T, String> displayFunction;
    private JDialog dialog;

    public ItemSearchAdditionComponent(Project project, 
                                     Function<String, List<T>> searchFunction,
                                     Function<T, String> displayFunction) {
        this.project = project;
        this.searchFunction = searchFunction;
        this.displayFunction = displayFunction;
        
        // Initialize models
        this.searchResultsModel = new DefaultListModel<>();
        this.candidateModel = new DefaultListModel<>();
        
        // Initialize components
        this.searchField = new JTextField();
        this.searchResultsList = new JBList<>(searchResultsModel);
        this.candidateList = new JBList<>(candidateModel);
        this.addButton = new JButton("Add →");
        this.removeButton = new JButton("← Remove");
        this.clearButton = new JButton("Clear All");
        this.confirmButton = new JButton("Confirm");
        
        // Set custom renderers for both lists
        setupListRenderers();
        
        // Create main panel
        this.mainPanel = new JPanel(new BorderLayout());
        setupUI();
        setupListeners();
    }

    private void setupListRenderers() {
        @SuppressWarnings("unchecked")
        ListCellRenderer<T> customRenderer = (ListCellRenderer<T>) new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                        int index, boolean isSelected, 
                                                        boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && component instanceof JLabel) {
                    @SuppressWarnings("unchecked")
                    T item = (T) value;
                    ((JLabel) component).setText(displayFunction.apply(item));
                }
                return component;
            }
        };
        
        searchResultsList.setCellRenderer(customRenderer);
        candidateList.setCellRenderer(customRenderer);
    }

    private void setupUI() {
        // Search panel at the top
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(JBUI.Borders.empty(5));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Center panel with search results and candidate list
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = JBUI.insets(5);

        // Search results panel
        JPanel searchResultsPanel = new JPanel(new BorderLayout());
        searchResultsPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));
        searchResultsPanel.add(new JBScrollPane(searchResultsList), BorderLayout.CENTER);
        centerPanel.add(searchResultsPanel, gbc);

        // Buttons panel between lists
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridy = 0;
        buttonGbc.insets = JBUI.insets(5);
        buttonPanel.add(addButton, buttonGbc);
        buttonGbc.gridy = 1;
        buttonPanel.add(removeButton, buttonGbc);
        centerPanel.add(buttonPanel, gbc);

        // Candidate list panel
        JPanel candidatePanel = new JPanel(new BorderLayout());
        candidatePanel.setBorder(BorderFactory.createTitledBorder("Selected Items"));
        candidatePanel.add(new JBScrollPane(candidateList), BorderLayout.CENTER);
        centerPanel.add(candidatePanel, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with clear and confirm buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(clearButton);
        bottomPanel.add(confirmButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        // Search field listener
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });

        // Add button listener
        addButton.addActionListener(e -> {
            List<T> selectedItems = searchResultsList.getSelectedValuesList();
            for (T item : selectedItems) {
                if (!candidateModel.contains(item)) {
                    candidateModel.addElement(item);
                }
            }
        });

        // Remove button listener
        removeButton.addActionListener(e -> {
            List<T> selectedItems = candidateList.getSelectedValuesList();
            for (T item : selectedItems) {
                candidateModel.removeElement(item);
            }
        });

        // Clear button listener
        clearButton.addActionListener(e -> {
            candidateModel.clear();
        });

        // Confirm button listener
        confirmButton.addActionListener(e -> {
            if (dialog != null) {
                dialog.dispose();
            }
        });
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            List<T> results = searchFunction.apply(searchText);
            searchResultsModel.clear();
            for (T result : results) {
                searchResultsModel.addElement(result);
            }
        }
    }

    public void showDialog(String title) {
        Window window = WindowManager.getInstance().suggestParentWindow(project);
        if (window == null) {
            window = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }

        if (window instanceof Frame) {
            dialog = new JDialog((Frame) window, title, true);
        } else {
            dialog = new JDialog((Dialog) window, title, true);
        }
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
    }

    public List<T> getSelectedItems() {
        List<T> items = new ArrayList<>();
        for (int i = 0; i < candidateModel.size(); i++) {
            items.add(candidateModel.getElementAt(i));
        }
        return items;
    }

    public void setSearchResults(List<T> results) {
        searchResultsModel.clear();
        for (T result : results) {
            searchResultsModel.addElement(result);
        }
    }

    public void clearSearchResults() {
        searchResultsModel.clear();
    }

    public void clearCandidates() {
        candidateModel.clear();
    }
}

