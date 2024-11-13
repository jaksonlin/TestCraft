package com.github.jaksonlin.pitestintellij.completion

import com.github.jaksonlin.pitestintellij.annotations.DefaultValue
import com.github.jaksonlin.pitestintellij.annotations.ValidationMode
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.github.jaksonlin.pitestintellij.ui.CustomAnnotationCompletionLookupElement
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
                        val isDefault = when (field.defaultValue) {
                            is DefaultValue.StringValue -> field.defaultValue.value == value
                            is DefaultValue.StringListValue -> value in field.defaultValue.value
                            else -> false
                        }

                        val element = CustomAnnotationCompletionLookupElement(
                            value = value,
                            fieldType = field.type,
                            isDefaultValue = isDefault
                        )

                        val prioritized = when {
                            isDefault -> PrioritizedLookupElement.withPriority(element, 100.0)
                            field.validation.mode == ValidationMode.EXACT ->
                                PrioritizedLookupElement.withPriority(element, 50.0)
                            else -> PrioritizedLookupElement.withPriority(element, 0.0)
                        }

                        result.addElement(prioritized)
                    }

                }


            }
        )
    }
}