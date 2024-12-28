package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DefaultValue {
    @Serializable
    @SerialName("StringValue")
    data class StringValue(val value: String) : DefaultValue()
    
    @Serializable
    @SerialName("StringListValue")
    data class StringListValue(val value: List<String>) : DefaultValue()
    
    @Serializable
    @SerialName("NullValue")
    object NullValue : DefaultValue()
}


@Serializable
data class AnnotationFieldConfig(
    val name: String,
    val type: AnnotationFieldType,
    val required: Boolean = false,
    val defaultValue: DefaultValue = DefaultValue.NullValue,
    val validation: FieldValidation? = null,
    val valueProvider: ValueProvider? = null
)