package com.github.jaksonlin.pitestintellij.actions;

import com.github.jaksonlin.pitestintellij.commands.unittestannotations.CheckAnnotationCommand;
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext;
import com.github.jaksonlin.pitestintellij.util.Pair;
import com.github.jaksonlin.pitestintellij.util.PsiUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;


public class RunCaseAnnoationCheckAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Pair<PsiMethod, PsiClass> psiMethodInfo = PsiUtil.findMethodAtCaret(e);
        if (psiMethodInfo == null) {
            return;
        }
        CaseCheckContext context = CaseCheckContext.create(psiMethodInfo.getKey(), psiMethodInfo.getValue());
        Project project = e.getProject();
        if (project != null) {
            CheckAnnotationCommand command = new CheckAnnotationCommand(project, context);
            command.execute();
        }
    }

}