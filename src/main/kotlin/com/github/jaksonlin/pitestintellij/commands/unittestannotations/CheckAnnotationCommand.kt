package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.annotations.AnnotationFieldType
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.context.UnittestCase
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class CheckAnnotationCommand(project: Project,  context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    override fun execute() {
        val annotation = findTargetAnnotation(context.psiMethod, context.schema)
        if (annotation == null) {
            showNoAnnotationMessage(project, context.schema.annotationClassName)
            return
        }
        processAnnotation(annotation)
        return
    }



    private fun processAnnotation(
        annotation: PsiAnnotation
    ) {
        try {
            val testCase = parseUnittestCaseFromAnnotations(annotation)
            val message = formatTestCaseMessage(testCase, context.schema)
            showSuccessMessage(project, message)
        } catch (e: Exception) {
            showErrorMessage(project, e.message ?: "Unknown error")
        }
    }

    private fun extractMethodBodyComments(psiMethod: PsiMethod): String {
        val stepComments = mutableListOf<String>()
        val assertComments = mutableListOf<String>()

        PsiTreeUtil.findChildrenOfType(psiMethod, PsiComment::class.java).forEach {
            if (it.text.contains("step", ignoreCase = true)) {
                stepComments.add(it.text)
            }
            if (it.text.contains("assert", ignoreCase = true)) {
                assertComments.add(it.text)
            }
        }
        // format as : test_step_1: step_comment, remove the leading //
        return stepComments.mapIndexed { index, comment -> "test_step_${index + 1}: ${comment.substring(2)}" }.joinToString("\n") + "\n" + assertComments.mapIndexed { index, comment -> "test_assert_${index + 1}: ${comment.substring(2)}" }.joinToString("\n")
    }


    private fun formatTestCaseMessage(
        testCase: UnittestCase,
        schema: AnnotationSchema
    ): String = buildString {
        appendLine("Test Case Details:")
        schema.fields.forEach { field ->
            append(field.name)
            append(": ")
            when (field.type) {
                AnnotationFieldType.STRING ->
                    appendLine(testCase.getString(field.name))
                AnnotationFieldType.STRING_LIST ->
                    appendLine(testCase.getStringList(field.name).joinToString(", "))
            }
        }
        appendLine(extractMethodBodyComments(context.psiMethod))
    }

}