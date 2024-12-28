package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class CheckMethodDataCommand(project: Project, context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    override fun execute() {
        val annotation = findTargetAnnotation(context.psiMethod, context.schema)
        if (annotation == null) {
            showNoAnnotationMessage(project, context.schema.annotationClassName)
            return
        }
        val comments = extractCommentsFromMethodBody(context.psiMethod)
        if (comments.isEmpty()) {
            showErrorMessage(project, "No comments found in the method body")
            return
        }
    }

    private fun extractCommentsFromMethodBody(psiMethod: PsiMethod): List<String> {
        val comments = mutableListOf<String>()
        PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment::class.java).forEach {
            comments.add(it.text)
        }
        return comments
    }
}