package com.github.jaksonlin.testcraft.presentation.components.system;

import com.github.jaksonlin.testcraft.infrastructure.services.business.RunHistoryManagerService;
import com.github.jaksonlin.testcraft.presentation.components.llmchat.LLMSuggestionUIComponent;
import com.github.jaksonlin.testcraft.presentation.components.mutation.MutationToolWindowUIComponent;
import com.github.jaksonlin.testcraft.presentation.components.testcase.InvalidTestCasesResultComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTabbedPane;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;

import javax.swing.*;

public class ToolWindowMainComponent {
    private final Project project;
    private final ToolWindow toolWindow;
    private final JBTabbedPane tabbedPane;

    public ToolWindowMainComponent(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.tabbedPane = new JBTabbedPane();
        initializeTabs();
    }

    private void initializeTabs() {
        
        // create a new LLMSuggestionUIComponent
        LLMSuggestionUIComponent uiComponent = new LLMSuggestionUIComponent();
        // add the uiComponent to the toolWindow through the content manager
        JPanel llmSuggestionToolWindowPanel = uiComponent.getPanel();
        tabbedPane.addTab(I18nService.getInstance().message("toolwindow.llm.suggestion.tab.name"), llmSuggestionToolWindowPanel);
        RunHistoryManagerService.getInstance().addObserver(uiComponent);

        // create a new MutationToolWindowUIComponent
        MutationToolWindowUIComponent mutationToolWindowUI = new MutationToolWindowUIComponent(project);
        JPanel mutationToolWindowPanel = mutationToolWindowUI.getPanel();
        tabbedPane.addTab(I18nService.getInstance().message("toolwindow.mutation.tab.name"), mutationToolWindowPanel);

        // create a new InvalidTestCasesResultComponent
        InvalidTestCasesResultComponent invalidTestCasesResultComponent = new InvalidTestCasesResultComponent();
        JPanel invalidTestCasesResultPanel = invalidTestCasesResultComponent.getPanel();
        tabbedPane.addTab(I18nService.getInstance().message("toolwindow.invalid.testcases.tab.name"), invalidTestCasesResultPanel);
    }

    

    public JComponent getContent() {
        return tabbedPane;
    }
} 