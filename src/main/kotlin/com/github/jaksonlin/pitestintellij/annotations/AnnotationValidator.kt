package com.github.jaksonlin.pitestintellij.annotations

class AnnotationValidator(private val schema: AnnotationSchema) {
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }

    fun validate(annotationValues: Map<String, Any>): ValidationResult {
        val errors = mutableListOf<String>()

        // Check required fields
        schema.fields
            .filter { it.required }
            .forEach { field ->
                if (!annotationValues.containsKey(field.name) || annotationValues[field.name] == null) {
                    errors.add("Missing required field: ${field.name}")
                }
            }

        // Validate field types
        annotationValues.forEach { (name, value) ->
            schema.fields.find { it.name == name }?.let { field ->
                validateFieldType(field, value)?.let { error ->
                    errors.add(error)
                }
            }
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    private fun validateFieldType(field: AnnotationFieldConfig, value: Any?): String? {
        if (value == null) return null

        return when (field.type) {
            AnnotationFieldType.STRING -> {
                if (value !is String) "Field ${field.name} must be a String"
                else null
            }
            AnnotationFieldType.STRING_LIST -> {
                when (value) {
                    is List<*> -> {
                        if (value.any { it !is String }) {
                            "Field ${field.name} must be a list of Strings"
                        } else null
                    }
                    else -> "Field ${field.name} must be a List"
                }
            }
            AnnotationFieldType.STATUS -> {
                if (value !is String) {
                    "Field ${field.name} must be a String"
                } else {
                    val validStatus = setOf("TODO", "IN_PROGRESS", "DONE", "DEPRECATED", "BROKEN") // Add your valid statuses
                    if (!validStatus.contains(value.uppercase())) {
                        "Invalid status value for ${field.name}: $value"
                    } else null
                }
            }
        }
    }
}