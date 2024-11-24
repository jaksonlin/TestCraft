package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ValueProvider(
    val type: ValueProviderType,
    val format: String? = null,
    val value: JsonElement? = null
)

@Serializable
enum class ValueProviderType {
    GIT_AUTHOR,
    LAST_MODIFIER_AUTHOR,
    LAST_MODIFIER_TIME,
    CURRENT_DATE,
    METHOD_NAME_BASED,
    FIXED_VALUE,
    CLASS_NAME,
    METHOD_NAME,
    METHOD_SIGNATURE
}