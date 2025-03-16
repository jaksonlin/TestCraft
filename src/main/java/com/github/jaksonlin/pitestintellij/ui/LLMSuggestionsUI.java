package com.github.jaksonlin.pitestintellij.ui;

import com.github.jaksonlin.pitestintellij.components.LLMResponsePanel;
import com.github.jaksonlin.pitestintellij.viewmodels.LLMSuggestionsPanelViewModel;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class LLMSuggestionsUI {
    private final Project project;
    private final LLMSuggestionsPanelViewModel vm;
    private final LLMResponsePanel mainPanel = new LLMResponsePanel();
    public LLMSuggestionsUI(Project project) {
        this.project = project;
        this.vm = new LLMSuggestionsPanelViewModel(project, mainPanel);
    }

    public JPanel getPanel() {
        return this.mainPanel;
    }
} 