package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.Serializable

@Serializable
data class FieldValidation(
    val validValues: List<String> = emptyList(),
    val allowCustomValues: Boolean = true,
    val minLength: Int? = null,  // Added for list length validation
    val maxLength: Int? = null   // Optional: add max length constraint
)
