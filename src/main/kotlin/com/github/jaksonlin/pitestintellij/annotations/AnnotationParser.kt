package com.github.jaksonlin.pitestintellij.annotations

import com.github.jaksonlin.pitestintellij.context.UnittestCase


class AnnotationParser(private val schema: AnnotationSchema) {
    private val validator = AnnotationValidator(schema)

    fun parseAnnotation(annotationValues: Map<String, Any>): UnittestCase {
        when (val result = validator.validate(annotationValues)) {
            is AnnotationValidator.ValidationResult.Valid -> {
                val parsedValues = schema.fields.associate { field ->
                    if (field.required) {
                        if (!annotationValues.containsKey(field.name)) {
                            throw IllegalArgumentException("Missing required field: ${field.name}")
                        }
                        if (annotationValues[field.name] == null) {
                            throw IllegalArgumentException("Required field cannot be null: ${field.name}")
                        }
                    } else {
                        if (!annotationValues.containsKey(field.name) || annotationValues[field.name] == null) {
                            return@associate field.name to field.defaultValue
                        }
                    }
                    val rawValue = annotationValues[field.name]
                    field.name to convertValue(rawValue, field)
                }
                return UnittestCase(parsedValues)
            }
            is AnnotationValidator.ValidationResult.Invalid -> {
                throw IllegalArgumentException(
                    "Invalid annotation values:\n${result.errors.joinToString("\n")}"
                )
            }
        }
    }

    private fun convertValue(value: Any?, field: AnnotationFieldConfig): Any? {
        if (value == null) {
            return when (val defaultValue = field.defaultValue) {
                is DefaultValue.StringValue -> defaultValue.value
                is DefaultValue.StringListValue -> defaultValue.value
                DefaultValue.NullValue -> null
            }
        }

        return when (field.type) {
            AnnotationFieldType.STRING -> value as? String ?: field.defaultValue
            AnnotationFieldType.STRING_LIST -> (value as? List<*>)?.mapNotNull { it as? String } ?: emptyList<String>()
            AnnotationFieldType.STATUS -> (value as? String)?.uppercase() ?: field.defaultValue
        }
    }
}