package com.github.jaksonlin.testcraft.application.processors;

import com.github.jaksonlin.testcraft.core.context.UnittestMethodContext;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

public class UnittestMethodProcessor {
    public static UnittestMethodContext fromPsiMethod(PsiMethod psiMethod) {
        List<String> comments = extractCommentsFromMethodBody(psiMethod);
        return new UnittestMethodContext(psiMethod.getName(), comments);
    }

    private static List<String> extractCommentsFromMethodBody(PsiMethod psiMethod) {
        List<String> comments = new ArrayList<>();
        for (PsiComment comment : PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment.class)) {
            comments.add(comment.getText());
        }
        return comments;
    }
}