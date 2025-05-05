package com.github.jaksonlin.testcraft.infrastructure.commands.unittestannotations;

import com.github.jaksonlin.testcraft.domain.annotations.AnnotationFieldType;
import com.github.jaksonlin.testcraft.domain.annotations.AnnotationSchema;
import com.github.jaksonlin.testcraft.infrastructure.commands.testscan.UnittestCaseCheckCommand;
import com.github.jaksonlin.testcraft.domain.context.CaseCheckContext;
import com.github.jaksonlin.testcraft.domain.context.UnittestCase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckAnnotationCommand extends UnittestCaseCheckCommand {

    public CheckAnnotationCommand(Project project, CaseCheckContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        PsiAnnotation annotation = findTargetAnnotation(getContext().getPsiMethod(), getContext().getSchema());
        if (annotation == null) {
            showNoAnnotationMessage(getProject(), getContext().getSchema().getAnnotationClassName());
            return;
        }
        processAnnotation(annotation);
    }

    private void processAnnotation(PsiAnnotation annotation) {
        try {
            UnittestCase testCase = parseUnittestCaseFromAnnotations(annotation);
            String message = formatTestCaseMessage(testCase, getContext().getSchema());
            showSuccessMessage(getProject(), message);
        } catch (Exception e) {
            showErrorMessage(getProject(), e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    private String extractMethodBodyComments(PsiMethod psiMethod) {
        List<String> stepComments = new ArrayList<>();
        List<String> assertComments = new ArrayList<>();

        PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment.class).forEach(comment -> {
            if (comment.getText().toLowerCase().contains("step")) {
                stepComments.add(comment.getText());
            }
            if (comment.getText().toLowerCase().contains("assert")) {
                assertComments.add(comment.getText());
            }
        });

        String stepCommentsFormatted = stepComments.stream()
                .map(comment -> {
                    int index = stepComments.indexOf(comment);
                    return "test_step_" + (index + 1) + ": " + comment.substring(2);
                })
                .collect(Collectors.joining("\n"));

        String assertCommentsFormatted = assertComments.stream()
                .map(comment -> {
                    int index = assertComments.indexOf(comment);
                    return "test_assert_" + (index + 1) + ": " + comment.substring(2);
                })
                .collect(Collectors.joining("\n"));

        return stepCommentsFormatted + (stepCommentsFormatted.isEmpty() ? "" : "\n") + assertCommentsFormatted;
    }

    private String formatTestCaseMessage(UnittestCase testCase, AnnotationSchema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("Test Case Details:\n");
        schema.getFields().forEach(field -> {
            sb.append(field.getName()).append(": ");
            if (field.getType() == AnnotationFieldType.STRING) {
                sb.append(testCase.getString(field.getName())).append("\n");
            } else if (field.getType() == AnnotationFieldType.STRING_LIST) {
                sb.append(String.join(", ", testCase.getStringList(field.getName()))).append("\n");
            }
        });
        sb.append(extractMethodBodyComments(getContext().getPsiMethod())).append("\n");
        return sb.toString();
    }
}
