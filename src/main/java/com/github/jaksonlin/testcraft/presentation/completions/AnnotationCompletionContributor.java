package com.github.jaksonlin.testcraft.presentation.completions;

import com.github.jaksonlin.testcraft.core.annotations.AnnotationFieldConfig;
import com.github.jaksonlin.testcraft.core.annotations.AnnotationSchema;
import com.github.jaksonlin.testcraft.core.annotations.DefaultValue;
import com.github.jaksonlin.testcraft.core.services.AnnotationConfigService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;


public class AnnotationCompletionContributor extends CompletionContributor {
    private static final Logger LOG = Logger.getInstance(AnnotationCompletionContributor.class);

    public AnnotationCompletionContributor() {
        LOG.info("Initializing AnnotationCompletionContributor");
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .inside(PsiAnnotation.class)
                        .withLanguage(JavaLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(parameters.getPosition(), PsiAnnotation.class);
                        if (annotation == null) {
                            return;
                        }
                        LOG.info("Found annotation: " + annotation.getQualifiedName());
                        PsiNameValuePair nameValuePair = PsiTreeUtil.getParentOfType(parameters.getPosition(), PsiNameValuePair.class);
                        if (nameValuePair == null) {
                            return;
                        }

                        LOG.info("Found attribute: " + nameValuePair.getName());

                        if (nameValuePair == null || annotation == null) {
                            LOG.info("Required PSI elements not found");
                            return;
                        }
                        AnnotationConfigService configService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);
                        AnnotationSchema schema = configService.getSchema();
                        LOG.info("Schema annotation class: " + schema.getAnnotationClassName());
                        LOG.info("Actual annotation class: " + annotation.getQualifiedName());

                        if (annotation.getQualifiedName() == null || !annotation.getQualifiedName().endsWith(schema.getAnnotationClassName())) {
                            LOG.info("Annotation mismatch: " + annotation.getQualifiedName() + " not the same as " + schema.getAnnotationClassName());
                            return;
                        }

                        String fieldName = nameValuePair.getName();
                        LOG.info("Field name: " + fieldName);

                        AnnotationFieldConfig field = null;
                        for (AnnotationFieldConfig f : schema.getFields()) {
                            if (f.getName().equals(fieldName)) {
                                field = f;
                                break;
                            }
                        }
                        if (field == null) {
                            LOG.info("Field not found in schema");
                            return;
                        }

                        AnnotationFieldConfig finalField = field;
                        field.getValidation().getValidValues().forEach(value -> {
                            boolean isDefault = false;
                            if (finalField.getDefaultValue() instanceof DefaultValue.StringValue) {
                                isDefault = ((DefaultValue.StringValue) finalField.getDefaultValue()).getValue().equals(value);
                            } else if (finalField.getDefaultValue() instanceof DefaultValue.StringListValue) {
                                isDefault = ((DefaultValue.StringListValue) finalField.getDefaultValue()).getValue().contains(value);
                            }

                            CustomAnnotationCompletionLookupElement element = new CustomAnnotationCompletionLookupElement(
                                    value,
                                    finalField.getType(),
                                    isDefault
                            );
                            double prioritizedValue;
                            if (result.getPrefixMatcher().prefixMatches(value)) {
                                prioritizedValue = 100.0;
                            } else if (isDefault) {
                                prioritizedValue = 90.0;
                            } else if (finalField.getValidation().getValidValues().contains(value)) {
                                prioritizedValue = 80.0;
                            } else {
                                prioritizedValue = 70.0;
                            }
                            LookupElement prioritized = PrioritizedLookupElement.withPriority(element, prioritizedValue);
                            LOG.info("Adding element: " + value + " with priority " + prioritizedValue);

                            result.addElement(prioritized);
                        });
                    }
                }
        );
    }
}
