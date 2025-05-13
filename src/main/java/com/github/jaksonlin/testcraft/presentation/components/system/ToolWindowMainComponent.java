package com.github.jaksonlin.testcraft.presentation.components.system;

import com.github.jaksonlin.testcraft.infrastructure.services.business.RunHistoryManagerService;
import com.github.jaksonlin.testcraft.presentation.components.llmchat.LLMSuggestionUIComponent;
import com.github.jaksonlin.testcraft.presentation.components.mutation.MutationToolWindowUIComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTabbedPane;

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
        tabbedPane.addTab("LLM Suggestions", llmSuggestionToolWindowPanel);
        RunHistoryManagerService.getInstance().addObserver(uiComponent);

        MutationToolWindowUIComponent mutationToolWindowUI = new MutationToolWindowUIComponent(project);
        JPanel mutationToolWindowPanel = mutationToolWindowUI.getPanel();
        tabbedPane.addTab("Mutation", mutationToolWindowPanel);
    }

    

    public JComponent getContent() {
        return tabbedPane;
    }
} 