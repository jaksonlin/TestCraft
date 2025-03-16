package com.github.jaksonlin.pitestintellij.actions;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.license.PremiumManager;
import com.github.jaksonlin.pitestintellij.services.LLMService;
import com.github.jaksonlin.pitestintellij.services.RunHistoryManager;
import com.github.jaksonlin.pitestintellij.toolWindow.LLMSuggestionsDialog;
import com.intellij.openapi.actionSystem.AnAction;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
public class GenerateUnitTestAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        if (!PremiumManager.getInstance().isPremium()) {
//            Messages.showInfoMessage(
//                    "This feature requires a premium license. Please upgrade to use the LLM unittest suggestions.",
//                    "Premium Feature"
//            );
//            return;
//        }

        Project project = e.getProject();
        if (project == null) return;

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return;

        RunHistoryManager historyManager = project.getService(RunHistoryManager.class);

        PitestContext context = historyManager.getRunHistoryForClassByTargetFilePath(psiFile.getVirtualFile().getPath());
        if (context == null) {
            Messages.showWarningDialog(
                    "No mutation test results found for this class. Please run mutation tests first.",
                    "No Results Found"
            );
            return;
        }

        LLMService llmService = project.getService(LLMService.class);
        String suggestions = llmService.generateUnitTestSuggestions(context);

        new LLMSuggestionsDialog(project, suggestions).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable/disable the action based on context
        e.getPresentation().setEnabledAndVisible(true);
    }
}