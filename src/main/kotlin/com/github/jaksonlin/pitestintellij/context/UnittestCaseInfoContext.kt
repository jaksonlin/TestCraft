package com.github.jaksonlin.pitestintellij.context

import com.github.jaksonlin.pitestintellij.annotations.UnittestAnnotationConfig

data class UnittestCaseInfoContext(
    val annotationValues: Map<String, Any>,
    private val config: UnittestAnnotationConfig = UnittestAnnotationConfig()
) {
    val author: String get() = annotationValues[config.authorField] as? String ?: ""
    val title: String get() = annotationValues[config.titleField] as? String ?: ""
    val targetClass: String get() = annotationValues[config.targetClassField] as? String ?: ""
    val targetMethod: String get() = annotationValues[config.targetMethodField] as? String ?: ""
    val testPoints: List<String> get() = (annotationValues[config.testPointsField] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    var status: UnittestCaseStatus = UnittestCaseStatus.TODO
    var description: String = annotationValues[config.descriptionField] as? String ?: ""
    var tags: List<String> = (annotationValues[config.tagsField] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    var relatedRequirements: List<String> = (annotationValues[config.relatedRequirementsField] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    var relatedDefects: List<String> = (annotationValues[config.relatedDefectsField] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
}

