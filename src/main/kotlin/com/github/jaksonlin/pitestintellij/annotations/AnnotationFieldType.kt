package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.Serializable

@Serializable
enum class AnnotationFieldType {
    STRING,
    STRING_LIST,
    STATUS
}