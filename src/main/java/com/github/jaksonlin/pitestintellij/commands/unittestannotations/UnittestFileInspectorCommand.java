package com.github.jaksonlin.pitestintellij.commands.unittestannotations;

import com.github.jaksonlin.pitestintellij.context.CaseCheckContext;
import com.github.jaksonlin.pitestintellij.services.InvalidAssertionConfigService;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Objects;
import java.util.Stack;

public class UnittestFileInspectorCommand extends UnittestCaseCheckCommand {
    private final ProblemsHolder holder;

    private final InvalidAssertionConfigService invalidAssertionConfigService = ApplicationManager.getApplication().getService(InvalidAssertionConfigService.class);
    public UnittestFileInspectorCommand(ProblemsHolder holder, Project project, CaseCheckContext context) {
        super(project, context);
        this.holder = holder;
    }

    @Override
    public void execute() {
        if (invalidAssertionConfigService.isEnable()) {
            checkAnnotationSchema(getContext().getPsiMethod());
            checkIfCommentHasStepAndAssert(getContext().getPsiMethod());
            checkIfthereIsAssertionStatement(getContext().getPsiMethod());
            checkIfValidAssertionStatement(getContext().getPsiMethod());
        }
    }

    private void checkAnnotationSchema(PsiMethod psiMethod) {
        try {
            PsiAnnotation annotation = findTargetAnnotation(psiMethod, getContext().getSchema());
            if (annotation == null) {
                holder.registerProblem(getContext().getPsiMethod(), "No unittest case management annotation found", ProblemHighlightType.WARNING);
                return;
            }
            parseUnittestCaseFromAnnotations(annotation);
        } catch (Exception e) {
            holder.registerProblem(getContext().getPsiMethod(), e.getMessage() != null ? e.getMessage() : "Unknown error", ProblemHighlightType.ERROR);
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
            holder.registerProblem(psiMethod, "Method should contains both step and assert comment", ProblemHighlightType.ERROR);
        }
    }

    private void checkIfthereIsAssertionStatement(PsiMethod psiMethod) {
        boolean hasAssertion = false;
        // Find all method calls within the method
        if (hasAssertionStatement(psiMethod)) {
            return;
        }

        // check if the method has annotation to assert the throw of exception: @Test(expected = Exception)
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            // 1. find the @Test annotation
            if (Objects.requireNonNull(annotation.getQualifiedName()).contains("Test")) {
                // 2. check if the annotation has "expected" attribute
                if (annotation.findAttributeValue("expected") != null && !Objects.requireNonNull(annotation.findAttributeValue("expected")).getText().equals("org.junit.Test.None.class")) {
                    hasAssertion = true;
                    break;
                }
            }
        }

        if (!hasAssertion) {
            holder.registerProblem(psiMethod, "Method should contains assert statement", ProblemHighlightType.ERROR);
        }
    }

    // check if a test method has assertion statement
    private boolean hasAssertionStatement(PsiMethod psiMethod) {
        Stack<PsiMethod> stack = new Stack<>();
        stack.push(psiMethod);
        int depth = 0;
        while (!stack.isEmpty()) {
            PsiMethod method = stack.pop();
            // 1. iterate all the method call in the method body
            for (PsiMethodCallExpression methodCall : PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)) {
                // 2. check if the method is assert statement
                String methodName = methodCall.getMethodExpression().getReferenceName();
                if (methodName != null && (methodName.toLowerCase().contains("assert") || methodName.toLowerCase().contains("verify"))) {
                    return true;
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
                return false;
            }
            // 5. if the stack is empty, return false
            if (stack.isEmpty()) {
                return false;
            }
            depth++;
        }

        return false;
    }
    

    private void checkIfValidAssertionStatement(PsiMethod psiMethod) {
        // check method call statement to see if there's any assert statement that is not valid (listed in invalidAssertions)
        for (PsiMethod method : PsiTreeUtil.findChildrenOfType(psiMethod, PsiMethod.class)) {
            for (String invalidAssertion : invalidAssertionConfigService.getInvalidAssertions()) {
                if (method.getText().contains(invalidAssertion)) {
                    holder.registerProblem(psiMethod, "Method should contains valid assert statement", ProblemHighlightType.ERROR);
                    return;
                }
            }
        }
    }
}