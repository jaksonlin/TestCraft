package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.annotations.DefaultValue
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiMethod

class GenerateAnnotationCommand(project: Project, context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    private val psiElementFactory: PsiElementFactory = JavaPsiFacade.getInstance(project).elementFactory
    override fun execute() {
        val annotation = findTargetAnnotation(context.psiMethod, context.schema)
        if (annotation != null) {
            showAnnotationAlreadyExistMessage(project, context.schema.annotationClassName)
            return
        }
        generateAnnotation(context.psiMethod, context.schema)
    }

    protected fun generateAnnotation(psiMethod: PsiMethod, schema: AnnotationSchema) {
        WriteCommandAction.runWriteCommandAction(project) {
            val annotation = buildAnnotation(schema)
            psiMethod.modifierList.addAfter(annotation, null)
        }
    }

    private fun buildAnnotation(schema: AnnotationSchema): PsiAnnotation {
        // Build the annotation text with default values
        val requiredFields = schema.fields.filter { it.required }
        
        val fieldValues = requiredFields.joinToString(",\n    ") { field ->
            val defaultValueStr = when (val default = field.defaultValue) {
                is DefaultValue.StringValue -> "\"${default.value}\""
                is DefaultValue.StringListValue -> {
                    val values = default.value.joinToString("\", \"") { "\"$it\"" }
                    if (values.isEmpty()) "{}" else "{$values}"
                }
                is DefaultValue.NullValue -> "null"
                null -> "\"\""  // Empty string for required fields without default
            }
            "${field.name} = $defaultValueStr"
        }

        val annotationText = if (fieldValues.isEmpty()) {
            "@${schema.annotationClassName}"
        } else {
            """@${schema.annotationClassName}(
    $fieldValues
)"""
        }

        return psiElementFactory.createAnnotationFromText(annotationText, null)
    }
}