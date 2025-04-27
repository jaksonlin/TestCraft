package com.github.jaksonlin.testcraft.commands.unittestannotations;

import com.github.jaksonlin.testcraft.MyBundle;
import com.github.jaksonlin.testcraft.annotations.AnnotationSchema;
import com.github.jaksonlin.testcraft.context.CaseCheckContext;
import com.github.jaksonlin.testcraft.context.UnittestCase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UnittestCaseCheckCommand {
    private final Project project;
    private final CaseCheckContext context;

    public UnittestCaseCheckCommand(Project project, CaseCheckContext context) {
        this.project = project;
        this.context = context;
    }

    public Project getProject() {
        return project;
    }

    public CaseCheckContext getContext() {
        return context;
    }

    public abstract void execute();

    public void showSuccessMessage(Project project, String message) {
        Messages.showMessageDialog(
                project,
                message,
                MyBundle.message("test.annotation.details.title"),
                Messages.getInformationIcon()
        );
    }

    public void showErrorMessage(Project project, String message) {
        Messages.showMessageDialog(
                project,
                "Error parsing annotation: " + message,
                MyBundle.message("test.file.action.title"),
                Messages.getErrorIcon()
        );
    }

    public void showNoAnnotationMessage(Project project, String annotationName) {
        Messages.showMessageDialog(
                project,
                "No " + annotationName + " annotation found on this method",
                MyBundle.message("test.file.action.title"),
                Messages.getWarningIcon()
        );
    }

    public void showNotJunitTestMethodMessage(Project project) {
        Messages.showMessageDialog(
                project,
                "This method is not a JUnit test method",
                MyBundle.message("test.annotation.generation.title"),
                Messages.getWarningIcon()
        );
    }

    public void showNoMethodMessage(Project project) {
        Messages.showMessageDialog(
                project,
                "No methods found in this class",
                MyBundle.message("test.methods.not.found.title"),
                Messages.getWarningIcon()
        );
    }

    public void showNoTestMethodCanAddMessage(Project project) {
        Messages.showMessageDialog(
                project,
                "No test methods found in the class that can add annotation.",
                MyBundle.message("test.methods.no.annotation.title"),
                Messages.getInformationIcon()
        );
    }

    public void showAnnotationAlreadyExistMessage(Project project, String annotationName) {
        Messages.showMessageDialog(
                project,
                annotationName + " already exist on this method",
                MyBundle.message("test.annotation.exists.title"),
                Messages.getWarningIcon()
        );
    }

    public PsiAnnotation findTargetAnnotation(
            PsiMethod psiMethod,
            AnnotationSchema schema
    ) {
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            if (annotation.getQualifiedName() != null && annotation.getQualifiedName().contains(schema.getAnnotationClassName())) {
                return annotation;
            }
        }
        return null;
    }

    public List<String> extractStringArrayValue(PsiAnnotation annotation, String attributeName) {
        PsiArrayInitializerMemberValue attributeValue = (PsiArrayInitializerMemberValue) annotation.findAttributeValue(attributeName);
        if (attributeValue != null) {
            List<String> values = new ArrayList<>();
            for (PsiAnnotationMemberValue initializer : attributeValue.getInitializers()) {
                if (initializer instanceof PsiLiteralExpression) {
                    String text = ((PsiLiteralExpression) initializer).getText();
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        values.add(text.substring(1, text.length() - 1));
                    }
                }
            }
            return values;
        } else {
            return java.util.Collections.emptyList();
        }
    }

    public void showValidationErrors(Project project, List<String> errors) {
        StringBuilder message = new StringBuilder("Annotation validation failed:\n");
        for (String error : errors) {
            message.append("- ").append(error).append("\n");
        }
        Messages.showMessageDialog(
                project,
                message.toString(),
                "Validation Errors",
                Messages.getErrorIcon()
        );
    }

    public Map<String, Object> parseAnnotationValues(PsiAnnotation annotation) {
        Map<String, Object> values = new HashMap<>();
        for (PsiNameValuePair attribute : annotation.getParameterList().getAttributes()) {
            String attributeName = attribute.getName();
            PsiAnnotationMemberValue value = attribute.getValue();
            if (value instanceof PsiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue @NotNull [] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();
                List<String> stringValues = new ArrayList<>();
                for (PsiAnnotationMemberValue initializer : initializers) {
                    String text = initializer.getText();
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        stringValues.add(text.substring(1, text.length() - 1));
                    }
                }
                values.put(attributeName, stringValues);
            } else {
                String text = value != null ? value.getText() : "";
                if (text.startsWith("\"") && text.endsWith("\"")) {
                    text = text.substring(1, text.length() - 1);
                }
                values.put(attributeName, text);
            }
        }
        return values;
    }

    public UnittestCase parseUnittestCaseFromAnnotations(PsiAnnotation annotation) {
        Map<String, Object> annotationValues = parseAnnotationValues(annotation);
        return context.getParser().parseAnnotation(annotationValues); 
    }


}
