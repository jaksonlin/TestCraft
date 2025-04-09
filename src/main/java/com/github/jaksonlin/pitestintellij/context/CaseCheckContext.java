package com.github.jaksonlin.pitestintellij.context;

import com.github.jaksonlin.pitestintellij.annotations.AnnotationParser;
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema;
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService;
import com.github.jaksonlin.pitestintellij.services.InvalidAssertionConfigService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.List;

public class CaseCheckContext {
    private final PsiClass psiClass;
    private final PsiMethod psiMethod;
    private final AnnotationSchema schema;
    private final AnnotationParser parser;
    private final List<String> invalidAssertions;

    public CaseCheckContext(PsiClass psiClass, PsiMethod psiMethod, AnnotationSchema schema, AnnotationParser parser, List<String> invalidAssertions) {
        this.psiClass = psiClass;
        this.psiMethod = psiMethod;
        this.schema = schema;
        this.parser = parser;
        this.invalidAssertions = invalidAssertions;
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
        return new CaseCheckContext(psiClass, newPsiMethod, schema, parser, invalidAssertions);
    }

    public static CaseCheckContext create(PsiMethod psiMethod, PsiClass psiClass) {
        AnnotationConfigService configService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);
        AnnotationSchema schema = configService.getSchema();
        InvalidAssertionConfigService invalidAssertionConfigService = ApplicationManager.getApplication().getService(InvalidAssertionConfigService.class);
        return new CaseCheckContext(
                psiClass,
                psiMethod,
                schema,
                new AnnotationParser(schema),
                invalidAssertionConfigService.getInvalidAssertions()
        );
    }
}