package com.github.jaksonlin.pitestintellij.annotations

data class UnittestAnnotationConfig(
    val authorField: String = "author",
    val titleField: String = "title",
    val targetClassField: String = "targetClass",
    val targetMethodField: String = "targetMethod",
    val testPointsField: String = "testPoints",
    val statusField: String = "status",
    val descriptionField: String = "description",
    val tagsField: String = "tags",
    val relatedRequirementsField: String = "relatedRequirements",
    val relatedDefectsField: String = "relatedDefects"
)