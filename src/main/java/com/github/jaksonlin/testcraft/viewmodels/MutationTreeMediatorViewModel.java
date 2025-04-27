package com.github.jaksonlin.testcraft.viewmodels;

import com.github.jaksonlin.testcraft.context.PitestContext;
import com.github.jaksonlin.testcraft.mediators.IMutationMediator;
import com.github.jaksonlin.testcraft.mediators.IMutationReportUI;
import com.github.jaksonlin.testcraft.services.RunHistoryManagerService;
import com.github.jaksonlin.testcraft.util.Mutation;
import com.github.jaksonlin.testcraft.util.Pair;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MutationTreeMediatorViewModel implements IMutationReportUI {
    private static final Logger log = LoggerFactory.getLogger(MutationTreeMediatorViewModel.class);
    private final Project project;
    private final IMutationMediator mediator;
    private final RunHistoryManagerService runHistoryManager;
    protected final HashMap<String, Integer> annotatedNodes = new HashMap<>();

    public MutationTreeMediatorViewModel(@NotNull Project project, @NotNull IMutationMediator mediator) {
        this.project = project;
        this.mediator = mediator;
        this.runHistoryManager = project.getService(RunHistoryManagerService.class);
        mediator.register(this);
        registerEditorListener(project);
    }

    private void registerEditorListener(Project project) {
        MessageBusConnection connect = project.getMessageBus().connect();
        connect.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                // Remove the node from the annotatedNodes set when the editor is closed
                if (annotatedNodes.containsKey(file.getPath())) {
                    annotatedNodes.remove(file.getPath());
                }
            }
        });
    }

    @Override
    public void updateMutationResult(String mutationClassFilePath, Map<Integer, Pair<String, Boolean>> mutationTestResult) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(mutationClassFilePath);
        if (virtualFile != null) {
            ApplicationManager.getApplication().invokeLater(() -> {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                fileEditorManager.openFile(virtualFile, true);
                FileEditor fileEditor = fileEditorManager.getSelectedEditor(virtualFile);
                if (fileEditor instanceof TextEditor) {
                    Editor editor = ((TextEditor) fileEditor).getEditor();
                    addMutationMarkers(editor, mutationTestResult);
                }
            }, ModalityState.defaultModalityState());
        }
    }

    public void handleOpenSelectedNode(DefaultMutableTreeNode selectedNode) {
        TreePath treePath = new TreePath(selectedNode.getPath());
        Object[] path = treePath.getPath();
        String[] classes = new String[path.length - 1];
        for (int i = 1; i < path.length; i++) {
            classes[i - 1] = path[i].toString();
        }
        String selectedClass = String.join(".", classes);

        PitestContext context = runHistoryManager.getRunHistoryForClass(selectedClass);
        if (context == null) {
            return; // if for any reason the class is not in the history, we do nothing
        }
        // if the file is already annotated, we switch to it
        if (annotatedNodes.containsKey(context.getTargetClassFilePath())) {
            switchToSelectedFile(context.getTargetClassFilePath());
            return;
        }
        // if the file is not annotated, we annotate it and open the file
        openClassFileAndAnnotate(context);
        annotatedNodes.put(context.getTargetClassFilePath(), 1);
    }

    private void switchToSelectedFile(String targetFilePath) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile currentFile = fileEditorManager.getSelectedFiles().length > 0 ? fileEditorManager.getSelectedFiles()[0] : null;
        if (currentFile == null || !Objects.equals(currentFile.getPath(), targetFilePath)) {
            VirtualFile targetFile = LocalFileSystem.getInstance().findFileByPath(targetFilePath);
            if (targetFile != null) {
                fileEditorManager.openFile(targetFile, true);
            }
        }
    }

    private boolean isEditorOpen(String className) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
        PitestContext historyForClass = runHistoryManager.getRunHistoryForClass(className);
        if (historyForClass == null) {
            return false;
        }
        String fileNameToCheck = historyForClass.getTargetClassFilePath();
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(fileNameToCheck);
        if (virtualFile == null) {
            return false;
        }
        for (VirtualFile openFile : openFiles) {
            if (openFile.getPath().contains(virtualFile.getPath())) {
                return true;
            }
        }
        return false;
    }

    private void openClassFileAndAnnotate(PitestContext context) {
        List<Mutation> mutations = context.getMutationResults();
        mediator.processMutationResult(context.getTargetClassFilePath(), mutations);
    }

    @Nullable
    private VirtualFile openClassFile(String filePath) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (virtualFile != null) {
            ApplicationManager.getApplication().invokeLater(() ->
                    FileEditorManager.getInstance(project).openFile(virtualFile, true), ModalityState.defaultModalityState()
            );
        }
        return virtualFile;
    }

    private void addMutationMarkers(Editor editor, Map<Integer, Pair<String, Boolean>> mutationTestResult) {
        MarkupModel markupModel = editor.getMarkupModel();

        for (Map.Entry<Integer, Pair<String, Boolean>> entry : mutationTestResult.entrySet()) {
            Integer lineNumber = entry.getKey();
            Pair<String, Boolean> mutationData = entry.getValue();
            String mutationDescription = mutationData.getFirst();
            boolean allKilled = mutationData.getSecond();
            Icon icon = null;
            if (allKilled) {
                icon = AllIcons.General.InspectionsOK;
            } else if (mutationDescription.contains("KILL")) {
                icon = AllIcons.General.Warning;
            } else {
                icon = AllIcons.General.Error;
            }

            RangeHighlighter highlighter = markupModel.addLineHighlighter(lineNumber - 1, 0, null);

            final Icon finalIcon = icon; // Make effectively final for lambda
            highlighter.setGutterIconRenderer(new GutterIconRenderer() {
                @Override
                public @NotNull Icon getIcon() {
                    return finalIcon;
                }

                @Override
                public String getTooltipText() {
                    return mutationDescription;
                }

                @Override
                public boolean isNavigateAction() {
                    return true;
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) return true;
                    if (!(obj instanceof GutterIconRenderer)) return false;
                    GutterIconRenderer other = (GutterIconRenderer) obj;
                    return Objects.equals(finalIcon, other.getIcon()) &&
                            Objects.equals(getTooltipText(), other.getTooltipText());
                }

                @Override
                public int hashCode() {
                    return Objects.hash(finalIcon, getTooltipText());
                }
            });
        }
    }
}