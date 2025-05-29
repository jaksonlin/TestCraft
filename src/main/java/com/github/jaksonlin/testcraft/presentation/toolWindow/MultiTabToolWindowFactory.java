package com.github.jaksonlin.testcraft.presentation.toolWindow;

import com.github.jaksonlin.testcraft.infrastructure.services.business.RunHistoryManagerService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;
import com.github.jaksonlin.testcraft.presentation.components.llmchat.LLMSuggestionUIComponent;
import com.github.jaksonlin.testcraft.presentation.components.mutation.MutationToolWindowUIComponent;
import com.github.jaksonlin.testcraft.presentation.components.system.ToolWindowMainComponent;
import com.github.jaksonlin.testcraft.presentation.components.testcase.InvalidTestCasesResultComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MultiTabToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentManager contentManager = toolWindow.getContentManager();

        // Create mutation tab
        Content mutationContent = getMutationContent(project);
        contentManager.addContent(mutationContent);

        // Create llmSuggestion tab
        Content llmSuggestionContent = getLLMSuggestionContent();
        contentManager.addContent(llmSuggestionContent);

        // Create invalid test cases tab
        Content invalidTestCasesContent = getInvalidTestCasesContent();
        contentManager.addContent(invalidTestCasesContent);

    }

    private Content getLLMSuggestionContent() {
        LLMSuggestionUIComponent llmSuggestionUIComponent = new LLMSuggestionUIComponent();
        // add the uiComponent to the toolWindow through the content manager
        JPanel toolWindowPanel = llmSuggestionUIComponent.getPanel();

        Content content = new ContentImpl(toolWindowPanel, I18nService.getInstance().message("toolwindow.llm.suggestion.tab.name"), false); // Directly create ContentImpl

        // register the uiComponent to the runHistoryManagerService to sync the run history
        RunHistoryManagerService.getInstance().addObserver(llmSuggestionUIComponent);
        return content;
    }

    private Content getMutationContent(Project project) {
        // create a new MutationToolWindowUIComponent
        MutationToolWindowUIComponent mutationToolWindowUI = new MutationToolWindowUIComponent(project);
        JPanel mutationToolWindowPanel = mutationToolWindowUI.getPanel();
        Content content = new ContentImpl(mutationToolWindowPanel, I18nService.getInstance().message("toolwindow.mutation.tab.name"), false); // Directly create ContentImpl
        return content;
    }

    private Content getInvalidTestCasesContent() {
        // create a new InvalidTestCasesResultComponent
        InvalidTestCasesResultComponent invalidTestCasesResultComponent = new InvalidTestCasesResultComponent();
        JPanel invalidTestCasesResultPanel = invalidTestCasesResultComponent.getPanel();
        Content content = new ContentImpl(invalidTestCasesResultPanel, I18nService.getInstance().message("toolwindow.invalid.testcases.tab.name"), false); // Directly create ContentImpl
        return content;
    }
} 