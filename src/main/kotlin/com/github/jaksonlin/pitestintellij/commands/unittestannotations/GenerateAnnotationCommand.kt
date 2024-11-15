package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.annotations.DefaultValue
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

class GenerateAnnotationCommand(project: Project, context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    private val psiElementFactory: PsiElementFactory = JavaPsiFacade.getInstance(project).elementFactory
    private val configService = service<AnnotationConfigService>()

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

    private val LOG = Logger.getInstance(GenerateAnnotationCommand::class.java)

    private fun addImportIfNeeded(psiMethod: PsiMethod, annotationClassName: String) {
        val file = psiMethod.containingFile as? PsiJavaFile ?: return
        LOG.info("Processing file: ${file.name}")
        
        val importList = file.importList ?: return
        LOG.info("Current imports: ${importList.importStatements.joinToString { it.qualifiedName ?: "null" }}")
        
        val qualifiedName = "${configService.getAnnotationPackage()}.$annotationClassName"
        LOG.info("Trying to add import for: $qualifiedName")
        
        // Only add if not already imported
        if (importList.importStatements.none { it.qualifiedName == qualifiedName }) {
            LOG.info("Import not found, attempting to add")
            
            val project = file.project
            val facade = JavaPsiFacade.getInstance(project)
            val scope = GlobalSearchScope.allScope(project)
            
            LOG.info("Searching for class in global scope")
            val psiClass = facade.findClass(qualifiedName, scope)
            LOG.info("Found class: ${psiClass != null}")
            
            try {
                val importStatement = psiElementFactory.createImportStatement(
                    psiClass ?: return
                )
                LOG.info("Created import statement: ${importStatement.text}")
                
                importList.add(importStatement)
                LOG.info("Import added successfully")
            } catch (e: Exception) {
                LOG.error("Failed to add import", e)
            }
        } else {
            LOG.info("Import already exists")
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