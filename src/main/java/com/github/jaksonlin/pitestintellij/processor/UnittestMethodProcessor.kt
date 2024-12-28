package com.github.jaksonlin.pitestintellij.processor

import com.github.jaksonlin.pitestintellij.context.UnittestMethodContext
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class UnittestMethodProcessor {
    companion object {
        fun fromPsiMethod(psiMethod: PsiMethod): UnittestMethodContext {
            val comments = extractCommentsFromMethodBody(psiMethod)
            return UnittestMethodContext(psiMethod.name, comments)
        }

        private fun extractCommentsFromMethodBody(psiMethod: PsiMethod): List<String> {
            val comments = mutableListOf<String>()
            PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment::class.java).forEach {
                comments.add(it.text)
            }
            return comments
        }
    }
}