package com.github.jaksonlin.pitestintellij.annotations

data class AnnotationFieldConfig(
    val name: String,
    val type: AnnotationFieldType,
    val required: Boolean = false,
    val defaultValue: Any? = null
)