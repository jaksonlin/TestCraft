package com.github.jaksonlin.pitestintellij.actions;

import com.github.jaksonlin.pitestintellij.commands.unittestannotations.CheckAnnotationCommand;
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunTestFileAnnoationCheckAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        batchCheckAnnotation(e);
    }

    private void batchCheckAnnotation(@NotNull AnActionEvent e) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);
        if (psiJavaFile == null) {
            return;
        }

        psiJavaFile.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                // inspect the method annotations
                PsiAnnotation[] annotations = method.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    // inspect the annotation
                    String annotationName = annotation.getQualifiedName();
                    if (annotationName != null &&
                            (annotationName.equals("org.junit.Test") || annotationName.equals("org.junit.jupiter.api.Test") || annotationName.equals("Test"))) {
                        PsiClass psiClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
                        if (psiClass != null) {
                            CaseCheckContext context = CaseCheckContext.create(method, psiClass);
                            Project project = e.getProject();
                            if (project != null) {
                                new CheckAnnotationCommand(project, context).execute();
                                break;
                            }
                        }
                    }
                }
            }
        });
    }
}