package com.github.jaksonlin.testcraft.domain.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

public class InvalidTestCase {
    private final String qualifiedName;
    private final String methodName;
    private final String testCaseCode;
    private final Project project;
    private final PsiMethod psiMethod;
    private final PsiClass psiClass;
    private final String filePath;
    private final int offset;

    public InvalidTestCase(Project project, PsiClass psiClass, PsiMethod psiMethod) {
        this.project = project;
        this.psiMethod = psiMethod;
        this.psiClass = psiClass;
        this.qualifiedName = psiClass.getQualifiedName() + "#" + psiMethod.getName();
        this.methodName = psiMethod.getName();
        this.testCaseCode = psiMethod.getText();
        this.filePath = psiMethod.getContainingFile().getVirtualFile().getPath();
        this.offset = psiMethod.getTextOffset();
    }

    public Project getProject() {
        return project;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getOffset() {
        return offset;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getTestCaseCode() {
        return testCaseCode;
    }
}
