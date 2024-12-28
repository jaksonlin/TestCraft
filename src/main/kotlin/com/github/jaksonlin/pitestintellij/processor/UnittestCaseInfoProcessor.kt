package com.github.jaksonlin.pitestintellij.processor

import com.github.jaksonlin.pitestintellij.context.UnittestCaseInfoContext
import com.github.jaksonlin.pitestintellij.context.UnittestCaseStatus
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceExpression

class UnittestCaseInfoProcessor {
//    companion object {
//        fun fromPsiAnnotation(annotation: PsiAnnotation): UnittestCaseInfoContext {
//            val annotationResult = UnittestCaseInfoContext(
//                author = annotation.findAttributeValue("author")?.text ?: "",
//                title = annotation.findAttributeValue("title")?.text ?: "",
//                targetClass = annotation.findAttributeValue("targetClass")?.text ?: "",
//                targetMethod = annotation.findAttributeValue("targetMethod")?.text ?: "",
//                testPoints = extractStringArrayValue(annotation, "testPoints")
//            )
//
//            // process tags
//            annotationResult.tags = extractStringArrayValue(annotation, "tags")
//            // process relatedRequirement
//            annotationResult.relatedRequirements = extractStringArrayValue(annotation, "relatedRequirements")
//            // process relatedDefect
//            annotationResult.relatedDefects = extractStringArrayValue(annotation, "relatedDefects")
//
//            // process status enum
//            val statusValue = annotation.findAttributeValue("status")
//            if (statusValue is PsiReferenceExpression) {
//                annotationResult.status = UnittestCaseStatus.valueOf(statusValue.text.substringAfterLast("."))
//            }
//
//            // process description
//            val descriptionValue = annotation.findAttributeValue("description")?.text
//            if (descriptionValue != null) {
//                annotationResult.description = descriptionValue
//            }
//
//            return annotationResult
//        }
//
//        private fun extractStringArrayValue(annotation: PsiAnnotation, attributeName: String): List<String> {
//            val attributeValue = annotation.findAttributeValue(attributeName)
//            return if (attributeValue is PsiArrayInitializerMemberValue) {
//                attributeValue.initializers.mapNotNull { (it as? PsiLiteralExpression)?.value as? String }
//            } else {
//                emptyList()
//            }
//        }
//    }
}