package com.github.jaksonlin.testcraft.presentation.components.testcase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import com.github.jaksonlin.testcraft.domain.model.InvalidTestCase;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.InvalidTestScanEvent;
import com.github.jaksonlin.testcraft.infrastructure.messaging.events.TypedEventObserver;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.ui.components.JBList;

public class InvalidTestCasesResultComponent {

    private final TypedEventObserver<InvalidTestScanEvent> invalidTestScanEventObserver = new TypedEventObserver<InvalidTestScanEvent>(InvalidTestScanEvent.class) {
        @Override
        public void onTypedEvent(InvalidTestScanEvent event) {
            switch (event.getEventType()) {
                case InvalidTestScanEvent.INVALID_TEST_SCAN_START_EVENT:
                    summaryLabel.setText("Scanning for invalid test cases...");
                    break;
                case InvalidTestScanEvent.INVALID_TEST_SCAN_END_EVENT:
                    List<InvalidTestCase> invalidTestCases = (List<InvalidTestCase>) event.getPayload();
                    ApplicationManager.getApplication().invokeLater(() -> setInvalidTestCases(invalidTestCases));
                    break;
            }
        }
    };

    private final JPanel mainPanel = new JPanel();
    private final JLabel summaryLabel = new JLabel();
    private final JList<String> resultList = new JBList<>();
    private List<InvalidTestCase> currentInvalidTestCases = null;

    public InvalidTestCasesResultComponent() {
        initializePanel();
        setupDoubleClick();
    }

    private void initializePanel() {
        mainPanel.setLayout(new BorderLayout(10, 10));
        summaryLabel.setText("No invalid test cases found.");
        resultList.setListData(new String[0]);
        mainPanel.add(summaryLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultList), BorderLayout.CENTER);
    }

    /**
     * Call this to update the UI with new results.
     */
    public void setInvalidTestCases(List<InvalidTestCase> invalidTestCases) {
        this.currentInvalidTestCases = invalidTestCases;
        if (invalidTestCases == null || invalidTestCases.isEmpty()) {
            summaryLabel.setText("No invalid test cases found.");
            resultList.setListData(new String[0]);
        } else {
            summaryLabel.setText("Found " + invalidTestCases.size() + " invalid test cases:");
            String[] displayData = invalidTestCases.stream()
                .map(this::formatInvalidTestCase)
                .toArray(String[]::new);
            resultList.setListData(displayData);
        }
    }

    private void setupDoubleClick() {
        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && currentInvalidTestCases != null ) {
                    int index = resultList.locationToIndex(evt.getPoint());
                    if (index >= 0 && index < currentInvalidTestCases.size()) {
                        InvalidTestCase selected = currentInvalidTestCases.get(index);

                        navigateToInvalidTestCase(selected);
                    }
                }
            }
        });
    }

    private void navigateToInvalidTestCase(InvalidTestCase testCase) {
        Project project = testCase.getProject();
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(testCase.getFilePath());
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true);
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor != null) {
                CaretModel caretModel = editor.getCaretModel();
                caretModel.moveToOffset(testCase.getOffset());
                editor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER);
            }
        }
    }

    /**
     * Customize this method to display the details you want from InvalidTestCase.
     */
    private String formatInvalidTestCase(InvalidTestCase testCase) {
        return testCase.getQualifiedName();
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
