package com.github.jaksonlin.pitestintellij.commands.casecheck

import com.github.jaksonlin.pitestintellij.annotations.AnnotationFieldType
import com.github.jaksonlin.pitestintellij.annotations.AnnotationParser
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.context.UnittestCase
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiMethod

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
                AnnotationFieldType.STATUS ->
                    appendLine(testCase.getStatus(field.name))
            }
        }
    }

}