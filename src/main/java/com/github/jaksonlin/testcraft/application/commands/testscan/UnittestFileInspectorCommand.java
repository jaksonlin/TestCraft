package com.github.jaksonlin.testcraft.application.commands.testscan;

import com.github.jaksonlin.testcraft.core.context.CaseCheckContext;
import com.github.jaksonlin.testcraft.core.services.AnnotationConfigService;
import com.github.jaksonlin.testcraft.core.services.InvalidTestCaseConfigService;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

// this is inspector command for unittest file scan
public class UnittestFileInspectorCommand extends UnittestCaseCheckCommand {
    private final ProblemsHolder holder;
    private final InvalidTestCaseConfigService invalidTestCaseConfigService = ApplicationManager.getApplication().getService(InvalidTestCaseConfigService.class);
    private final AnnotationConfigService annotationConfigService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);

    public UnittestFileInspectorCommand(ProblemsHolder holder, Project project, CaseCheckContext context) {
        super(project, context);
        this.holder = holder;
    }

    private void reportError(String message, ProblemHighlightType highlightType) {
        if (holder == null) {
            return;
        }
        holder.registerProblem(getContext().getPsiMethod(), message, highlightType);
    }

    @Override
    public void execute() {
        if (annotationConfigService.shouldCheckAnnotation()) {
            checkAnnotationSchema(getContext().getPsiMethod());
        }

        // Only proceed with assertion and comment checks if the service is enabled
        if (invalidTestCaseConfigService.isEnable()) {
            // Check for assertion statements
            checkIfthereIsAssertionStatement(getContext().getPsiMethod());
            checkIfValidAssertionStatement(getContext().getPsiMethod());

            // Check for comments only if comment check is enabled
            if (invalidTestCaseConfigService.isEnableCommentCheck()) {
                checkIfCommentHasStepAndAssert(getContext().getPsiMethod());
            }
        }
    }

    private void checkAnnotationSchema(PsiMethod psiMethod) {
        try {
            PsiAnnotation annotation = findTargetAnnotation(psiMethod, getContext().getSchema());
            if (annotation == null) {
                reportError("No unittest case management annotation found", ProblemHighlightType.WARNING);
                return;
            }
            parseUnittestCaseFromAnnotations(annotation);
        } catch (Exception e) {
            reportError(e.getMessage() != null ? e.getMessage() : "Unknown error", ProblemHighlightType.WARNING);
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
            reportError("Method should contains both step and assert comment", ProblemHighlightType.WARNING);
        }
    }

    private void checkIfthereIsAssertionStatement(PsiMethod psiMethod) {

        // Find all method calls within the method
        if (hasAssertionStatement(psiMethod)) {
            return;
        }

        // check if the method has annotation to assert the throw of exception: @Test(expected = Exception)
        if (useTestExpectedAsAssertion(psiMethod)) {
            return;
        }

        reportError("Method should contains assert statement", ProblemHighlightType.ERROR);

    }

    private boolean useTestExpectedAsAssertion(PsiMethod psiMethod){
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            // 1. find the @Test annotation
            if (Objects.requireNonNull(annotation.getQualifiedName()).contains("Test")) {
                // 2. check if the annotation has "expected" attribute
                if (annotation.findAttributeValue("expected") != null && !Objects.requireNonNull(annotation.findAttributeValue("expected")).getText().equals("org.junit.Test.None.class")) {
                    return true;
                }
            }
        }
        return false;
    }

    // check if a test method has assertion statement
    private boolean hasAssertionStatement(PsiMethod psiMethod) {
       Optional<PsiMethod> assertionMethod = getAssertionMethodFromTestMethod(psiMethod);
       return assertionMethod.isPresent();
    }

    private Optional<PsiMethod> getAssertionMethodFromTestMethod(PsiMethod psiMethod) {
        Stack<PsiMethod> stack = new Stack<>();
        stack.push(psiMethod);
        int depth = 0;
        while (!stack.isEmpty()) {
            PsiMethod method = stack.pop();
            // 1. iterate all the method call in the method body
            for (PsiMethodCallExpression methodCall : PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)) {
                // 2. check if the method is assert statement
                String methodName = methodCall.getMethodExpression().getReferenceName();
                if (methodName != null && (methodName.toLowerCase().contains("assert")
                        || methodName.toLowerCase().contains("verify")
                        || methodName.toLowerCase().contains("fail"))) {
                    // which means the method contains assert statement
                    return Optional.of(method);
                } else {
                    // 3. if not, push the child method to the stack
                    // 3.1 convert the methodCall to a PsiMethod
                    PsiMethod childMethod = methodCall.resolveMethod();
                    if (childMethod != null) {
                        stack.push(childMethod);
                    }
                }
            }
            // 4. if the depth is greater than 3, return false
            if (depth > 3) {
                return Optional.empty();
            }
            // 5. if the stack is empty, return false
            if (stack.isEmpty()) {
                return Optional.empty();
            }
            depth++;
        }

        return Optional.empty();
    }


    private void checkIfValidAssertionStatement(PsiMethod psiMethod) {
        // check method call statement to see if there's any assert statement that is not valid (listed in invalidAssertions)
        Optional<PsiMethod> assertionMethod = getAssertionMethodFromTestMethod(psiMethod);
        // use method call as assertion statement, check the method content
        if (assertionMethod.isPresent()) {
            List<String> invalidAssertions = invalidTestCaseConfigService.getInvalidAssertions();
            String methodText = assertionMethod.get().getText();
            for (String invalidAssertion : invalidAssertions) {
                if (methodText.contains(invalidAssertion)) {
                    reportError( "Method should contains valid assert statement", ProblemHighlightType.ERROR);
                    return;
                }
            }
        }
    }
}