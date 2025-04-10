package com.github.jaksonlin.pitestintellij.viewmodels;

import com.intellij.openapi.project.Project;
import com.github.jaksonlin.pitestintellij.services.RunHistoryManagerService;

public class LLMSuggestionSelectorViewModel  {

    private final Project project;
    private final RunHistoryManagerService historyManager;

    public LLMSuggestionSelectorViewModel(Project project) {
        this.project = project;
        this.historyManager = project.getService(RunHistoryManagerService.class);
    }
} 