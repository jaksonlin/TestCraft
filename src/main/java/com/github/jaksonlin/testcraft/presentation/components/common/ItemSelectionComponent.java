package com.github.jaksonlin.testcraft.presentation.components.common;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.ItemSelectEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.TypedEventObserver;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ItemSelectionComponent<T> {

    private final TypedEventObserver<ItemSelectEvent> itemSelectEventObserver = new TypedEventObserver<ItemSelectEvent>(ItemSelectEvent.class) {
        @Override
        public void onTypedEvent(ItemSelectEvent event) {
            switch (event.getEventType()) {
                case ItemSelectEvent.ITEM_SELECT_EVENT_TYPE_OPEN:
                    List<T> candidateForSelection = (List<T>) event.getPayload();
                    // set the value for user to select
                    setCandidateForSelection(candidateForSelection);
            }
        }
    };

    private final JPanel mainPanel = new JPanel();
    private final JList<T> itemList = new JList<>();
    private final JButton selectAllButton = new JButton("Select All");
    private final JButton selectNoneButton = new JButton("Select None");
    private final JButton confirmButton = new JButton("Confirm");
    private String title;
    private JDialog dialog;
    private final Project project;

    public ItemSelectionComponent(Project project, String title) {
        this.project = project;
        this.title = title;
        setupUI();
        setupListeners();
    }


    private void setupUI() {
        mainPanel.setLayout(new BorderLayout());
        
        // Add title if provided
        if (title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(titleLabel, BorderLayout.NORTH);
        }

        // Add scroll pane for the list
        JScrollPane scrollPane = new JScrollPane(itemList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create a panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(selectAllButton);
        buttonPanel.add(selectNoneButton);
        buttonPanel.add(confirmButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        // select all button action listener
        selectAllButton.addActionListener(e -> {
            int size = itemList.getModel().getSize();
            if (size > 0) {
                itemList.setSelectionInterval(0, size - 1);
            }
        });
        
        // select none button action listener
        selectNoneButton.addActionListener(e -> {
            itemList.clearSelection();
        });

        // Add confirm button listener
        confirmButton.addActionListener(e -> {
            List<T> selectedItems = getSelectedItems();
            EventBusService.getInstance().post(new ItemSelectEvent(
                ItemSelectEvent.ITEM_SELECT_EVENT_TYPE_SELECT_ITEMS,
                selectedItems
            ));
            if (dialog != null) {
                dialog.dispose();
            }
        });
    }
    
    private void setCandidateForSelection(List<T> candidates) {
        itemList.setListData(candidates.toArray((T[]) new Object[0]));
        itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public List<T> getSelectedItems() {
        return itemList.getSelectedValuesList();
    }

    public void setTitle(String title) {
        this.title = title;
        if (title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(titleLabel, BorderLayout.NORTH);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void setItems(List<T> items) {
        setCandidateForSelection(items);
    }

    public void setButtonLabels(String selectAllLabel, String selectNoneLabel, String confirmLabel) {
        selectAllButton.setText(selectAllLabel);
        selectNoneButton.setText(selectNoneLabel);
        confirmButton.setText(confirmLabel);
    }

    public void showDialog() {
        Window window = WindowManager.getInstance().suggestParentWindow(project);
        if (window == null) {
            window = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        }
        
        if (window instanceof Frame) {
            dialog = new JDialog((Frame) window, title != null ? title : "Select Items", true);
        } else {
            dialog = new JDialog((Dialog) window, title != null ? title : "Select Items", true);
        }
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
    }
}
