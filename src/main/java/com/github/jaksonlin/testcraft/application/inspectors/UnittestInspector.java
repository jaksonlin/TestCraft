package com.github.jaksonlin.testcraft.application.inspectors;

import com.github.jaksonlin.testcraft.util.MyBundle;
import com.github.jaksonlin.testcraft.infrastructure.commands.testscan.UnittestFileInspectorCommand;
import com.github.jaksonlin.testcraft.domain.context.CaseCheckContext;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UnittestInspector extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull String getGroupDisplayName() {
        return MyBundle.message("inspection.group.name");
    }

    @Override
    public @NotNull String getDisplayName() {
        return MyBundle.message("inspection.display.name");
    }

    @Override
    public @NotNull String getShortName() {
        return "UnittestCaseAnnotationInspection";
    }

    protected final Set<String> testAnnotations = new HashSet<>(Arrays.asList(
        "org.junit.Test",
        "org.junit.jupiter.api.Test",
        "Test"
    ));

    private final Set<String> testClassAnnotations = new HashSet<>(Arrays.asList(
        "org.junit.runner.RunWith",
        "org.junit.jupiter.api.TestInstance",
        "org.junit.platform.suite.api.Suite"
    ));


    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        Project project = holder.getProject();
        return new JavaElementVisitor() {
            @Override
            public void visitMethod(PsiMethod psiMethod) {
                if (!hasTestAnnotation(psiMethod)) {
                    return;
                }

                PsiClass containingClass = psiMethod.getContainingClass();
                if (containingClass == null) {
                    return;
                }
                String qualifiedName = containingClass.getQualifiedName();
                if (qualifiedName == null) {
                    return;
                }
                PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);
                if (psiClass == null) {
                    return;
                }
                CaseCheckContext context = CaseCheckContext.create(psiMethod, psiClass);
                new UnittestFileInspectorCommand(holder, project, context).execute();
            }

            private boolean hasTestAnnotation(PsiMethod psiMethod) {
                for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
                    if (testAnnotations.contains(annotation.getQualifiedName())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    @Override
    public @NotNull String getID() {
        return "UnittestCaseAnnotationInspection";
    }

}
