package com.github.jaksonlin.pitestintellij.ui;

import com.github.jaksonlin.pitestintellij.components.ObservableTree;
import com.github.jaksonlin.pitestintellij.viewmodels.MutationToolWindowViewModel;
import com.intellij.openapi.project.Project;
import com.github.jaksonlin.pitestintellij.MyBundle;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MutationToolWindowUI {
    private final JButton clearButton = new JButton(MyBundle.message("clear.button"));
    private final JTextField searchInput = new JTextField(20);
    private final ObservableTree resultTree = new ObservableTree();
    private final MutationToolWindowViewModel vm;
    private final JPanel toolWindowPanel;

    public MutationToolWindowUI(Project project) {
        this.vm = new MutationToolWindowViewModel(project, resultTree);
        this.toolWindowPanel = createToolWindowPanel();
        registerListeners();
        // set the placeholder text
        searchInput.setToolTipText(MyBundle.message("search.placeholder"));
    }

    private void registerListeners() {
        clearButton.addActionListener(e -> vm.handleTreeClear());

        searchInput.addActionListener(e -> {
            String searchText = searchInput.getText();
            if (!searchText.isEmpty()) {
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) resultTree.getModel().getRoot();
                TreePath path = vm.handleSearchInTree(searchText, rootNode);
                if (path != null) {
                    resultTree.scrollPathToVisible(path);
                    resultTree.setSelectionPath(path);
                    resultTree.requestFocusInWindow(); // release focus from searchInput
                }
            }
        });

        resultTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) resultTree.getLastSelectedPathComponent();
                    vm.handleOpenSelectedNode(selectedNode);
                }
            }
        });

        // handle key enter event
        resultTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) resultTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        vm.handleOpenSelectedNode(selectedNode);
                    }
                }
            }
        });
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        controlPanel.add(clearButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        controlPanel.add(searchInput, gbc);

        return controlPanel;
    }

    private JPanel createToolWindowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controlPanel = createControlPanel();
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(resultTree, BorderLayout.CENTER);
        return panel;
    }

    public JPanel getPanel() {
        return toolWindowPanel;
    }
}