package com.github.jaksonlin.testcraft.presentation.components;

import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PitestOutputDialog extends DialogWrapper {

    private final String output;
    private final File reportFile;

    public PitestOutputDialog(Project project, String output, String dialogTitle, File reportFile) {
        super(project);
        this.output = output;
        this.reportFile = reportFile;
        setTitle(dialogTitle);
        init();
    }

    public PitestOutputDialog(Project project, String output, String dialogTitle) {
        this(project, output, dialogTitle, null);
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(output);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        panel.add(scrollPane, BorderLayout.CENTER);

        if (reportFile != null) {
            JButton viewReportButton = new JButton(I18nService.getInstance().message("pitest.view.report"));
            viewReportButton.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(reportFile.toURI());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            panel,
                            "Error opening report: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });
            panel.add(viewReportButton, BorderLayout.SOUTH);
        }

        return panel;
    }
}
