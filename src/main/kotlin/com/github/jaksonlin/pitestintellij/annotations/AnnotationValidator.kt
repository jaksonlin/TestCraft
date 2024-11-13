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
                validateField(field, value)?.let { error ->
                    errors.add(error)
                }
            }
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    private fun validateField(field: AnnotationFieldConfig, value: Any?): String? {
        if (value == null) return null

        // First validate the type
        val typeError = validateType(field, value)
        if (typeError != null) return typeError

        // Then validate non-empty if required
        val emptyError = validateNonEmpty(field, value)
        if (emptyError != null) return emptyError

        // Then validate against the validation rules if they exist
        field.validation?.let { validation ->
            when (value) {
                is String -> {
                    validateStringValue(field.name, value, validation)?.let { return it }
                }
                is List<*> -> {
                    // Validate list content
                    validateListContent(field.name, value, validation)?.let { return it }
                    
                    // Validate list length
                    validateListLength(field.name, value, validation)?.let { return it }
                }

                else -> {
                    // Should not happen
                    return "Unsupported field type for ${field.name}: ${value::class.simpleName}"
                }
            }
        }

        return null
    }

    private fun validateNonEmpty(field: AnnotationFieldConfig, value: Any): String? {
        if (!field.validation?.allowEmpty.let { it != null && !it }) {
            when (value) {
                is String -> {
                    if (value.isBlank()) {
                        return "Field ${field.name} cannot be empty"
                    }
                }
                is List<*> -> {
                    if (value.isEmpty()) {
                        return "Field ${field.name} cannot be empty"
                    }
                }
            }
        }
        return null
    }

    private fun validateType(field: AnnotationFieldConfig, value: Any): String? {
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
                if (value !is String) "Field ${field.name} must be a String"
                else null
            }
        }
    }

    private fun validateStringValue(
        fieldName: String,
        value: String,
        validation: FieldValidation
    ): String? {
        if (!validation.allowEmpty && value.isBlank()) {
            return "Field $fieldName cannot be empty"
        }
        if (validation.validValues.isNotEmpty() && !validation.allowCustomValues) {
            val isValid = when (validation.mode) {
                ValidationMode.EXACT -> validation.validValues.contains(value)
                ValidationMode.CONTAINS -> validation.validValues.any { candidate -> 
                    value.contains(candidate, ignoreCase = true) 
                }
            }
            if (!isValid) {
                return "Invalid value for $fieldName: $value. Valid values are: ${validation.validValues.joinToString()}"
            }
        }
        return null
    }

    private fun validateListContent(
        fieldName: String,
        value: List<*>,
        validation: FieldValidation
    ): String? {
        if (validation.validValues.isNotEmpty() && !validation.allowCustomValues) {
            val invalidValues = value.filterIsInstance<String>()
                .filter { item ->
                    when (validation.mode) {
                        ValidationMode.EXACT -> !validation.validValues.contains(item)
                        ValidationMode.CONTAINS -> !validation.validValues.any { candidate -> 
                            item.contains(candidate, ignoreCase = true) 
                        }
                    }
                }
            if (invalidValues.isNotEmpty()) {
                return "Invalid values for $fieldName: ${invalidValues.joinToString()}. Valid values are: ${validation.validValues.joinToString()}"
            }
        }
        return null
    }

    private fun validateListLength(
        fieldName: String,
        value: List<*>,
        validation: FieldValidation
    ): String? {
        validation.allowEmpty.let { allowEmpty ->
            if (!allowEmpty && value.isEmpty()) {
                return "Field $fieldName cannot be empty"
            }
        }
        validation.minLength?.let { min ->
            if (value.size < min) {
                return "$fieldName must contain at least $min element${if (min > 1) "s" else ""}"
            }
        }
        validation.maxLength?.let { max ->
            if (value.size > max) {
                return "$fieldName cannot contain more than $max element${if (max > 1) "s" else ""}"
            }
        }
        return null
    }
}