package com.github.jaksonlin.pitestintellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.ElementPattern
import com.intellij.psi.util.PsiTreeUtil

class AnnotationCompletionContributor : CompletionContributor() {
    private val LOG = Logger.getInstance(AnnotationCompletionContributor::class.java)

    init {
        LOG.info("Initializing AnnotationCompletionContributor")
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(PsiAnnotation::class.java)     // Inside an annotation
                .withLanguage(JavaLanguage.INSTANCE),  // Ensure it's Java
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    // Find the containing annotation and name-value pair
                    val annotation = PsiTreeUtil.getParentOfType(parameters.position, PsiAnnotation::class.java) ?: return
                    val nameValuePair = PsiTreeUtil.getParentOfType(parameters.position, PsiNameValuePair::class.java) ?: return

                    LOG.info("Found annotation: ${annotation.qualifiedName}")
                    LOG.info("Found attribute: ${nameValuePair.name}")

                    // Rest of your completion logic...
                    if (nameValuePair == null || annotation == null) {
                        LOG.info("Required PSI elements not found")
                        return
                    }
                    val project = parameters.position.project
                    val configService = project.service<AnnotationConfigService>()
                    val schema = configService.getSchema()
                    LOG.info("Schema annotation class: ${schema.annotationClassName}")
                    LOG.info("Actual annotation class: ${annotation.qualifiedName}")

                    // Check if this is our target annotation
                    if (annotation.qualifiedName != schema.annotationClassName) {
                        LOG.info("Annotation mismatch")
                        return
                    }

                    // Find matching field in schema
                    val fieldName = nameValuePair.name
                    LOG.info("Field name: $fieldName")

                    val field = schema.fields.find { it.name == fieldName }
                    if (field == null) {
                        LOG.info("Field not found in schema")
                        return
                    }

                    // Add completion items
                    field.validation?.validValues?.forEach { value ->
                        LOG.info("Adding completion value: $value")
                        val element = LookupElementBuilder.create(value)
                            .withCaseSensitivity(false)
                            .withTypeText(field.type.toString())
                        result.addElement(element)
                    }

                }
            }
        )
    }
}