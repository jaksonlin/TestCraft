package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.annotations.DefaultValue
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class GenerateAnnotationCommand(project: Project, context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    private val psiElementFactory: PsiElementFactory = JavaPsiFacade.getInstance(project).elementFactory
    private val configService = project.service<AnnotationConfigService>()

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
            if (configService.isAutoImport()) {
                addImportIfNeeded(psiMethod, schema.annotationClassName)
            }
            val annotation = buildAnnotation(schema)
            psiMethod.modifierList.addAfter(annotation, null)
        }
    }

    private fun addImportIfNeeded(psiMethod: PsiMethod, annotationClassName: String) {
        val file = psiMethod.containingFile
        if (file !is PsiJavaFile) return

        val importList = file.importList ?: return
        val qualifiedName = "${configService.getAnnotationPackage()}.$annotationClassName"

        // Check if import already exists
        if (importList.importStatements.none { it.qualifiedName == qualifiedName }) {
            val importStatement = psiElementFactory.createImportStatement(
                JavaPsiFacade.getInstance(project).findClass(qualifiedName, psiMethod.resolveScope) ?: return
            )
            importList.add(importStatement)
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