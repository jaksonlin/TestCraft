package com.github.jaksonlin.testcraft.commands.unittestannotations;

import com.github.jaksonlin.testcraft.commands.testscan.UnittestCaseCheckCommand;
import com.github.jaksonlin.testcraft.context.CaseCheckContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

public class CheckMethodDataCommand extends UnittestCaseCheckCommand {

    public CheckMethodDataCommand(Project project, CaseCheckContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        PsiAnnotation annotation = findTargetAnnotation(getContext().getPsiMethod(), getContext().getSchema());
        if (annotation == null) {
            showNoAnnotationMessage(getProject(), getContext().getSchema().getAnnotationClassName());
            return;
        }
        List<String> comments = extractCommentsFromMethodBody(getContext().getPsiMethod());
        if (comments.isEmpty()) {
            showErrorMessage(getProject(), "No comments found in the method body");
            return;
        }
        // You can add further processing of the comments here if needed.
    }

    private List<String> extractCommentsFromMethodBody(PsiMethod psiMethod) {
        List<String> comments = new ArrayList<>();
        PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment.class).forEach(psiComment -> {
            comments.add(psiComment.getText());
        });
        return comments;
    }
}
