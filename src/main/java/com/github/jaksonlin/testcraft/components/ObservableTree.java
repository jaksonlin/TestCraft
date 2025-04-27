package com.github.jaksonlin.testcraft.components;

import com.github.jaksonlin.testcraft.MyBundle;
import com.github.jaksonlin.testcraft.observers.BasicEventObserver;
import com.github.jaksonlin.testcraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObservableTree extends JTree implements BasicEventObserver {

    @Override
    public void onEventHappen(String eventName,Object eventObj) {
        if (!eventName.equals("RUN_HISTORY")) {
            return;
        }
        if (eventObj == null) {
            initializeMutationTree(Collections.emptyList());
        } else if (eventObj instanceof Pair) {
            Pair<?, ?> pair = (Pair<?, ?>) eventObj;
            if (pair.getFirst() instanceof String && pair.getSecond() instanceof String) {
                updateMutationTree(new Pair<>((String) pair.getFirst(), (String) pair.getSecond()));
            }
        } else if (eventObj instanceof List) {
            List<?> list = (List<?>) eventObj;
            if (list.isEmpty()) {
                initializeMutationTree(Collections.emptyList());
            } else if (list.get(0) instanceof Pair) {
                List<Pair<String, String>> nodeList = (List<Pair<String, String>>) list;
                initializeMutationTree(nodeList);
            }
        }
    }

    private void initializeMutationTree(@NotNull List<Pair<String, String>> nodeNameList) {
        DefaultTreeModel treeModel = buildTreeModel(nodeNameList);
        setModel(treeModel);
    }

    private DefaultTreeModel buildTreeModel(@NotNull List<Pair<String, String>> nodeNameList) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(MyBundle.message("mutation.tree.root"));

        for (Pair<String, String> pair : nodeNameList) {
            String packageName = pair.getFirst();
            DefaultMutableTreeNode packageNode = getOrCreatePackageNode(root, packageName);
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pair.getSecond());
            packageNode.add(newNode);
        }

        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode getOrCreatePackageNode(@NotNull DefaultMutableTreeNode root, @NotNull String packageName) {
        DefaultMutableTreeNode currentNode = root;
        String[] packageParts = packageName.split("\\.");
        for (String packagePart : packageParts) {
            DefaultMutableTreeNode childNode = null;
            for (int i = 0; i < currentNode.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) currentNode.getChildAt(i);
                if (Objects.equals(child.getUserObject(), packagePart)) {
                    childNode = child;
                    break;
                }
            }

            if (childNode == null) {
                childNode = new DefaultMutableTreeNode(packagePart);
                currentNode.add(childNode);
            }
            currentNode = childNode;
        }
        return currentNode;
    }

    private void updateMutationTree(@NotNull Pair<String, String> pair) {
        DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        String packageName = pair.getFirst();
        DefaultMutableTreeNode packageNode = getOrCreatePackageNode(root, packageName);

        boolean alreadyExists = false;
        for (int i = 0; i < packageNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) packageNode.getChildAt(i);
            if (Objects.equals(child.getUserObject(), pair.getSecond())) {
                alreadyExists = true;
                break;
            }
        }

        if (alreadyExists) {
            return;
        }

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pair.getSecond());
        packageNode.add(newNode);
        SwingUtilities.invokeLater(() -> {
            expandPath(new TreePath(packageNode.getPath()));
            updateUI();
        });
    }


}
