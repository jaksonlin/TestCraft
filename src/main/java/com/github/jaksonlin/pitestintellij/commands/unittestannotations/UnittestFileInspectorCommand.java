package com.github.jaksonlin.pitestintellij.commands.unittestannotations;

import com.github.jaksonlin.pitestintellij.context.CaseCheckContext;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

public class UnittestFileInspectorCommand extends UnittestCaseCheckCommand {
    private final ProblemsHolder holder;

    public UnittestFileInspectorCommand(ProblemsHolder holder, Project project, CaseCheckContext context) {
        super(project, context);
        this.holder = holder;
    }

    @Override
    public void execute() {
        checkAnnotationSchema(getContext().getPsiMethod());
        checkIfCommentHasStepAndAssert(getContext().getPsiMethod());
    }

    private void checkAnnotationSchema(PsiMethod psiMethod) {
        try {
            PsiAnnotation annotation = findTargetAnnotation(psiMethod, getContext().getSchema());
            if (annotation == null) {
                holder.registerProblem(getContext().getPsiMethod(), "No unittest case management annotation found", ProblemHighlightType.WARNING);
                return;
            }
            parseUnittestCaseFromAnnotations(annotation);
        } catch (Exception e) {
            holder.registerProblem(getContext().getPsiMethod(), e.getMessage() != null ? e.getMessage() : "Unknown error", ProblemHighlightType.ERROR);
        }
    }

    private void checkIfCommentHasStepAndAssert(PsiMethod psiMethod) {
        boolean hasStep = false;
        boolean hasAssert = false;
        for (PsiComment comment : PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment.class)) {
            if (comment.getText().toLowerCase().contains("step")) {
                hasStep = true;
            }
            if (comment.getText().toLowerCase().contains("assert")) {
                hasAssert = true;
            }
        }
        if (!hasStep || !hasAssert) {
            holder.registerProblem(psiMethod, "Method should contains both step and assert comment", ProblemHighlightType.ERROR);
        }
    }
}