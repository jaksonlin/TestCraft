package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class UnittestFileInspectorCommand(private val holder: ProblemsHolder, project: Project, context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    override fun execute() {
        checkAnnotationSchema(context.psiMethod)
        checkIfCommentHasStepAndAssert(context.psiMethod)
    }

    private fun checkAnnotationSchema(psiMethod:PsiMethod) {
        try {
            val annotation = findTargetAnnotation(psiMethod, context.schema)
            if (annotation == null) {
                holder.registerProblem(context.psiMethod, "No unittest case management annotation found", ProblemHighlightType.WARNING)
                return
            }
            val testCase = parseUnittestCaseFromAnnotations(annotation)
        } catch (e: Exception) {
            holder.registerProblem(context.psiMethod, e.message ?: "Unknown error", ProblemHighlightType.ERROR)
        }
    }

    private fun checkIfCommentHasStepAndAssert(psiMethod: PsiMethod) {
        var hasStep = false
        var hasAssert = false
        PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment::class.java).forEach {
            //comments.add(it.text)
            if (it.text.contains("step", ignoreCase = true)) {
                hasStep = true
            }
            if (it.text.contains("assert", ignoreCase = true)) {
                hasAssert = true
            }
        }
        if (!hasStep || !hasAssert) {
            holder.registerProblem(psiMethod, "Method should contains both step and assert comment", ProblemHighlightType.ERROR)
        }
    }
}