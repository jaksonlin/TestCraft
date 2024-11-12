package com.github.jaksonlin.pitestintellij.commands.casecheck

import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.context.UnittestCaseInfoContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod

abstract class UnittestCaseCheckCommand(protected val project: Project, protected val context: CaseCheckContext) {
    abstract fun execute()
    fun showSuccessMessage(project: Project, message: String) {
        Messages.showMessageDialog(
            project,
            message,
            "Test Annotation Details",
            Messages.getInformationIcon()
        )
    }

    fun showErrorMessage(project: Project, message: String) {
        Messages.showMessageDialog(
            project,
            "Error parsing annotation: $message",
            "Test File Action",
            Messages.getErrorIcon()
        )
    }

    fun showNoAnnotationMessage(project: Project, annotationName: String) {
        Messages.showMessageDialog(
            project,
            "No $annotationName annotation found on this method",
            "Test File Action",
            Messages.getWarningIcon()
        )
    }

    fun findTargetAnnotation(
        psiMethod: PsiMethod,
        schema: AnnotationSchema
    ): PsiAnnotation? {
        return psiMethod.annotations.find { annotation ->
            annotation.qualifiedName?.contains(schema.annotationClassName) == true
        }
    }

    fun extractStringArrayValue(annotation: PsiAnnotation, attributeName: String): List<String> {
        val attributeValue = annotation.findAttributeValue(attributeName)
        return if (attributeValue is PsiArrayInitializerMemberValue) {
            // retrieve the array element into the tags
            //attributeValue.initializers.map { it.text }
            attributeValue.initializers.mapNotNull { (it as? PsiLiteralExpression)?.value as? String }
        } else {
            emptyList()
        }
    }

    fun showValidationErrors(project: Project, errors: List<String>) {
        val message = buildString {
            appendLine("Annotation validation failed:")
            errors.forEach { error ->
                appendLine("- $error")
            }
        }
        Messages.showMessageDialog(
            project,
            message,
            "Validation Errors",
            Messages.getErrorIcon()
        )
    }
}