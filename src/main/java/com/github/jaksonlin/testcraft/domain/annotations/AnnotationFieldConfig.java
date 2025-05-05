package com.github.jaksonlin.testcraft.domain.annotations;

import org.jetbrains.annotations.Nullable;

public class AnnotationFieldConfig {
    private String name;
    private AnnotationFieldType type;
    private boolean required;
    private DefaultValue defaultValue;
    @Nullable
    private FieldValidation validation;
    @Nullable
    private ValueProvider valueProvider;

    public AnnotationFieldConfig() {
        this.required = false;
        this.defaultValue = new DefaultValue.NullValue();
    }

    public AnnotationFieldConfig(String name, AnnotationFieldType type) {
        this.name = name;
        this.type = type;
        this.required = false;
        this.defaultValue = new DefaultValue.NullValue();
    }

    public AnnotationFieldConfig(String name, AnnotationFieldType type, FieldValidation validation) {
        this.name = name;
        this.type = type;
        this.required = false;
        this.defaultValue = new DefaultValue.NullValue();
        this.validation = validation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AnnotationFieldType getType() {
        return type;
    }

    public void setType(AnnotationFieldType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public DefaultValue getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(DefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Nullable
    public FieldValidation getValidation() {
        return validation;
    }

    public void setValidation(@Nullable FieldValidation validation) {
        this.validation = validation;
    }

    @Nullable
    public ValueProvider getValueProvider() {
        return valueProvider;
    }

    public void setValueProvider(@Nullable ValueProvider valueProvider) {
        this.valueProvider = valueProvider;
    }
}
