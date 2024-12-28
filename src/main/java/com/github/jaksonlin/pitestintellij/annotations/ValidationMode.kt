package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.Serializable

@Serializable
enum class ValidationMode {
    EXACT,      // Exact string match
    CONTAINS,   // String contains check
}