package com.github.jaksonlin.pitestintellij.inspectors;

import com.github.jaksonlin.pitestintellij.MyBundle;
import com.github.jaksonlin.pitestintellij.commands.unittestannotations.UnittestFileInspectorCommand;
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Set<String> testAnnotations = new HashSet<>(Arrays.asList(
        "org.junit.Test",
        "org.junit.jupiter.api.Test",
        "Test"
    ));

    private final Set<String> testClassAnnotations = new HashSet<>(Arrays.asList(
        "org.junit.runner.RunWith",
        "org.junit.jupiter.api.TestInstance",
        "org.junit.platform.suite.api.Suite"
    ));

    private final ConcurrentHashMap<String, Boolean> testClassCache = new ConcurrentHashMap<>();

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

    public void clearCache() {
        testClassCache.clear();
    }
}
