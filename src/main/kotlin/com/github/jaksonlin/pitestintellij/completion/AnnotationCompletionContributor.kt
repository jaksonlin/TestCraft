package com.github.jaksonlin.pitestintellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.ElementPattern

class AnnotationCompletionContributor : CompletionContributor() {
    private val LOG = Logger.getInstance(AnnotationCompletionContributor::class.java)

    init {
        LOG.info("Initializing AnnotationCompletionContributor")
        // Start with a very broad pattern
//         extend(
//             CompletionType.BASIC,
//             PlatformPatterns.psiElement(),  // Match any element
//             AnnotationCompletionProvider()
//         )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(PsiAnnotation::class.java),
            AnnotationCompletionProvider()
        )
        /* Once the first breakpoint is hit, we can gradually narrow it down:
        
        // Step 1: Match any element inside an annotation
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withSuperParent(2, PsiAnnotation::class.java),
            AnnotationCompletionProvider()
        )

        // Step 2: Match any element that's part of an annotation attribute
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withParent(PsiNameValuePair::class.java)
                .withSuperParent(2, PsiAnnotation::class.java),
            AnnotationCompletionProvider()
        )

        // Step 3: Match string literals in annotations
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withParent(PsiLiteralExpression::class.java)
                .withSuperParent(2, PsiNameValuePair::class.java)
                .withSuperParent(3, PsiAnnotation::class.java),
            AnnotationCompletionProvider()
        )
        */
    }

    // Override for debugging
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        LOG.info("fillCompletionVariants called")
        LOG.info("Position: ${parameters.position}")
        LOG.info("Original position: ${parameters.originalPosition}")
        LOG.info("Offset: ${parameters.offset}")
        LOG.info("Is auto popup: ${parameters.isAutoPopup}")

        // Log the PSI tree for the current position
        LOG.info("PSI tree:")
        var element: PsiElement? = parameters.position
        while (element != null) {
            LOG.info("  ${element.javaClass.simpleName}: '${element.text}'")
            element = element.parent
        }

        // Create a wrapped result set for debugging

        super.fillCompletionVariants(parameters, result)
    }

    private class AnnotationCompletionProvider : CompletionProvider<CompletionParameters>() {
        private val LOG = Logger.getInstance(AnnotationCompletionContributor::class.java)

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            LOG.info("addCompletions called")

            val position = parameters.position
            LOG.info("Position class: ${position.javaClass.name}")
            LOG.info("Position text: ${position.text}")

            val nameValuePair = position.parent?.parent as? PsiNameValuePair
            LOG.info("NameValuePair: ${nameValuePair?.text}")

            val annotation = nameValuePair?.parent?.parent as? PsiAnnotation
            LOG.info("Annotation: ${annotation?.text}")

            if (nameValuePair == null || annotation == null) {
                LOG.info("Required PSI elements not found")
                return
            }

            val configService = position.project.service<AnnotationConfigService>()
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
}