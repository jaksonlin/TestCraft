package com.github.jaksonlin.pitestintellij.annotations;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnnotationValidator {
    private final AnnotationSchema schema;

    public AnnotationValidator(AnnotationSchema schema) {
        this.schema = schema;
    }

    public static abstract class ValidationResult {
        public static class Valid extends ValidationResult {
        }

        public static class Invalid extends ValidationResult {
            private final List<String> errors;

            public Invalid(List<String> errors) {
                this.errors = errors;
            }

            public List<String> getErrors() {
                return errors;
            }
        }
    }

    public ValidationResult validate(Map<String, Object> annotationValues) {
        List<String> errors = new ArrayList<>();

        // Check required fields
        schema.getFields().stream()
                .filter(AnnotationFieldConfig::isRequired)
                .forEach(field -> {
                    if (!annotationValues.containsKey(field.getName()) || annotationValues.get(field.getName()) == null) {
                        errors.add("Missing required field: " + field.getName());
                    }
                });

        // Validate field types
        annotationValues.forEach((name, value) -> schema.getFields().stream()
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .ifPresent(field -> {
                    String error = validateField(field, value);
                    if (error != null) {
                        errors.add(error);
                    }
                }));

        return errors.isEmpty() ? new ValidationResult.Valid() : new ValidationResult.Invalid(errors);
    }

    @Nullable
    private String validateField(AnnotationFieldConfig field, @Nullable Object value) {
        if (value == null) return null;

        // First validate the type
        String typeError = validateType(field, value);
        if (typeError != null) return typeError;

        // Then validate non-empty if required
        String emptyError = validateNonEmpty(field, value);
        if (emptyError != null) return emptyError;

        // Then validate against the validation rules if they exist
        FieldValidation validation = field.getValidation();
        if (validation != null) {
            if (value instanceof String) {
                String error = validateStringValue(field.getName(), (String) value, validation);
                if (error != null) return error;
            } else if (value instanceof List<?>) {
                // Validate list content
                String contentError = validateListContent(field.getName(), (List<?>) value, validation);
                if (contentError != null) return contentError;

                // Validate list length
                String lengthError = validateListLength(field.getName(), (List<?>) value, validation);
                if (lengthError != null) return lengthError;
            } else {
                // Should not happen
                return "Unsupported field type for " + field.getName() + ": " + value.getClass().getSimpleName();
            }
        }

        return null;
    }

    @Nullable
    private String validateNonEmpty(AnnotationFieldConfig field, Object value) {
        FieldValidation validation = field.getValidation();
        if (validation != null && !validation.isAllowEmpty()) {
            if (value instanceof String) {
                if (((String) value).trim().isEmpty()) {
                    return "Field " + field.getName() + " cannot be empty";
                }
            } else if (value instanceof List<?>) {
                if (((List<?>) value).isEmpty()) {
                    return "Field " + field.getName() + " cannot be empty";
                }
            }
        }
        return null;
    }

    @Nullable
    private String validateType(AnnotationFieldConfig field, Object value) {
        switch (field.getType()) {
            case STRING:
                if (!(value instanceof String)) return "Field " + field.getName() + " must be a String";
                else return null;
            case STRING_LIST:
                if (value instanceof List<?>) {
                    for (Object item : (List<?>) value) {
                        if (!(item instanceof String)) {
                            return "Field " + field.getName() + " must be a list of Strings";
                        }
                    }
                    return null;
                } else return "Field " + field.getName() + " must be a List";
            default:
                return null;
        }
    }

    @Nullable
    private String validateStringValue(
            String fieldName,
            String value,
            FieldValidation validation
    ) {
        if (!validation.isAllowEmpty() && value.trim().isEmpty()) {
            return "Field " + fieldName + " cannot be empty";
        }
        if (!validation.getValidValues().isEmpty() && !validation.isAllowCustomValues()) {
            boolean isValid = false;
            switch (validation.getMode()) {
                case EXACT:
                    isValid = validation.getValidValues().contains(value);
                    break;
                case CONTAINS:
                    for (String candidate : validation.getValidValues()) {
                        if (value.toLowerCase().contains(candidate.toLowerCase())) {
                            isValid = true;
                            break;
                        }
                    }
                    break;
            }
            if (!isValid) {
                return "Invalid value for " + fieldName + ": " + value + ". Valid values are: " + String.join(", ", validation.getValidValues());
            }
        }
        return null;
    }

    @Nullable
    private String validateListContent(
            String fieldName,
            List<?> value,
            FieldValidation validation
    ) {
        if (!validation.getValidValues().isEmpty() && !validation.isAllowCustomValues()) {
            List<String> invalidValues = value.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(item -> {
                        switch (validation.getMode()) {
                            case EXACT:
                                return !validation.getValidValues().contains(item);
                            case CONTAINS:
                                return validation.getValidValues().stream()
                                        .noneMatch(candidate -> item.toLowerCase().contains(candidate.toLowerCase()));
                            default:
                                return false;
                        }
                    })
                    .collect(Collectors.toList());

            if (!invalidValues.isEmpty()) {
                return "Invalid values for " + fieldName + ": " + String.join(", ", invalidValues) + ". Valid values are: " + String.join(", ", validation.getValidValues());
            }
        }
        return null;
    }

    @Nullable
    private String validateListLength(
            String fieldName,
            List<?> value,
            FieldValidation validation
    ) {
        if (!validation.isAllowEmpty() && value.isEmpty()) {
            return "Field " + fieldName + " cannot be empty";
        }
        Integer min = validation.getMinLength();
        if (min != null && value.size() < min) {
            return fieldName + " must contain at least " + min + " element" + (min > 1 ? "s" : "");
        }
        Integer max = validation.getMaxLength();
        if (max != null && value.size() > max) {
            return fieldName + " cannot contain more than " + max + " element" + (max > 1 ? "s" : "");
        }
        return null;
    }
}