package com.github.jaksonlin.pitestintellij.completion

import com.github.jaksonlin.pitestintellij.annotations.DefaultValue
import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.github.jaksonlin.pitestintellij.ui.CustomAnnotationCompletionLookupElement
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
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
                    val annotation = PsiTreeUtil.getParentOfType(parameters.position, PsiAnnotation::class.java)
                    if (annotation == null){
                        return
                    }
                    LOG.info("Found annotation: ${annotation.qualifiedName}")
                    val nameValuePair = PsiTreeUtil.getParentOfType(parameters.position, PsiNameValuePair::class.java)
                    if (nameValuePair == null){
                        return
                    }


                    LOG.info("Found attribute: ${nameValuePair.name}")

                    // Rest of your completion logic...
                    if (nameValuePair == null || annotation == null) {
                        LOG.info("Required PSI elements not found")
                        return
                    }
                    val project = parameters.position.project
                    val configService = service<AnnotationConfigService>()
                    val schema = configService.getSchema()
                    LOG.info("Schema annotation class: ${schema.annotationClassName}")
                    LOG.info("Actual annotation class: ${annotation.qualifiedName}")

                    // Check if this is our target annotation
                    if (annotation.qualifiedName?.endsWith(schema.annotationClassName) != true) {
                        LOG.info("Annotation mismatch: ${annotation.qualifiedName} not the same as ${schema.annotationClassName}")
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
                        val isDefault = when (field.defaultValue) {
                            is DefaultValue.StringValue -> (field.defaultValue as DefaultValue.StringValue).value == value
                            is DefaultValue.StringListValue -> value in (field.defaultValue as DefaultValue.StringListValue).value
                            else -> false
                        }

                        val element = CustomAnnotationCompletionLookupElement(
                            value = value,
                            fieldType = field.type,
                            isDefaultValue = isDefault
                        )
                        var properitizedValue = 100.0
                        when {
                            // Highest priority for exact matches
                            result.prefixMatcher.prefixMatches(value) -> properitizedValue = 100.0
                            // High priority for default values
                            isDefault -> properitizedValue = 90.0
                            // Medium priority for validated values
                            field.validation?.validValues?.contains(value) == true -> properitizedValue = 80.0
                            // Lower priority for other suggestions
                            else -> properitizedValue = 70.0
                        }
                        val prioritized = PrioritizedLookupElement.withPriority(element,properitizedValue)
                        LOG.info("Adding element: $value with priority ${properitizedValue}")

                        result.addElement(prioritized)
                    }

                }


            }
        )
    }
}