package com.github.jaksonlin.pitestintellij.context;

import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService;
import com.github.jaksonlin.pitestintellij.annotations.AnnotationParser;
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

public class CaseCheckContext {
    private final PsiClass psiClass;
    private final PsiMethod psiMethod;
    private final AnnotationSchema schema;
    private final AnnotationParser parser;

    public CaseCheckContext(PsiClass psiClass, PsiMethod psiMethod, AnnotationSchema schema, AnnotationParser parser) {
        this.psiClass = psiClass;
        this.psiMethod = psiMethod;
        this.schema = schema;
        this.parser = parser;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public AnnotationSchema getSchema() {
        return schema;
    }

    public AnnotationParser getParser() {
        return parser;
    }

    public CaseCheckContext copy(PsiMethod newPsiMethod) {
        return new CaseCheckContext(psiClass, newPsiMethod, schema, parser);
    }

    public static CaseCheckContext create(PsiMethod psiMethod, PsiClass psiClass) {
        AnnotationConfigService configService = ServiceManager.getService(AnnotationConfigService.class);
        AnnotationSchema schema = configService.getSchema();
        return new CaseCheckContext(
                psiClass,
                psiMethod,
                schema,
                new AnnotationParser(schema)
        );
    }
}