package com.github.jaksonlin.testcraft.core.annotations;

import com.github.jaksonlin.testcraft.core.context.UnittestCase;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotationParser {
    private final AnnotationSchema schema;
    private final AnnotationValidator validator;

    public AnnotationParser(AnnotationSchema schema) {
        this.schema = schema;
        this.validator = new AnnotationValidator(schema);
    }

    public UnittestCase parseAnnotation(Map<String, Object> annotationValues) {
        AnnotationValidator.ValidationResult result = validator.validate(annotationValues);

        if (result instanceof AnnotationValidator.ValidationResult.Valid) {
            Map<String, Object> parsedValues = new HashMap<>();
            for (AnnotationFieldConfig field : schema.getFields()) {
                if (field.isRequired()) {
                    if (!annotationValues.containsKey(field.getName())) {
                        throw new IllegalArgumentException("Missing required field: " + field.getName());
                    }
                    if (annotationValues.get(field.getName()) == null) {
                        throw new IllegalArgumentException("Required field cannot be null: " + field.getName());
                    }
                } else {
                    if (!annotationValues.containsKey(field.getName()) || annotationValues.get(field.getName()) == null) {
                        parsedValues.put(field.getName(), field.getDefaultValue());
                        continue;
                    }
                }
                Object rawValue = annotationValues.get(field.getName());
                parsedValues.put(field.getName(), convertValue(rawValue, field));
            }
            return new UnittestCase(parsedValues);
        } else if (result instanceof AnnotationValidator.ValidationResult.Invalid) {
            AnnotationValidator.ValidationResult.Invalid invalidResult = (AnnotationValidator.ValidationResult.Invalid) result;
            throw new IllegalArgumentException(
                    "Invalid annotation values:\n" + String.join("\n", invalidResult.getErrors())
            );
        }
        // Should not happen if ValidationResult is properly implemented
        throw new IllegalStateException("Unexpected ValidationResult type");
    }

    @Nullable
    private Object convertValue(@Nullable Object value, AnnotationFieldConfig field) {
        if (value == null) {
            DefaultValue defaultValue = field.getDefaultValue();
            if (defaultValue instanceof DefaultValue.StringValue) {
                return ((DefaultValue.StringValue) defaultValue).getValue();
            } else if (defaultValue instanceof DefaultValue.StringListValue) {
                return ((DefaultValue.StringListValue) defaultValue).getValue();
            } else if (defaultValue instanceof DefaultValue.NullValue) {
                return null;
            }
            return null; // Should ideally not reach here if DefaultValue is exhaustive
        }

        switch (field.getType()) {
            case STRING:
                return value instanceof String ? value : (field.getDefaultValue() instanceof DefaultValue.StringValue ? ((DefaultValue.StringValue) field.getDefaultValue()).getValue() : null);
            case STRING_LIST:
                if (value instanceof List<?>) {
                    return ((List<?>) value).stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .collect(Collectors.toList());
                } else {
                    return java.util.Collections.emptyList();
                }
            default:
                return null; // Should not happen if AnnotationFieldType is exhaustive
        }
    }
}