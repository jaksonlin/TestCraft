package com.github.jaksonlin.testcraft.core.annotations;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FieldValidation {
    private List<String> validValues;
    private boolean allowCustomValues;
    @Nullable
    private Integer minLength;
    @Nullable
    private Integer maxLength;
    private ValidationMode mode;
    private boolean allowEmpty;

    public FieldValidation() {
        this.validValues = java.util.Collections.emptyList();
        this.allowCustomValues = true;
        this.mode = ValidationMode.EXACT;
        this.allowEmpty = true;
    }



    public FieldValidation(List<String> validValues, boolean allowCustomValues, ValidationMode mode) {
        this.validValues = validValues;
        this.allowCustomValues = allowCustomValues;
        this.mode = mode;
    }

    public List<String> getValidValues() {
        return validValues;
    }

    public void setValidValues(List<String> validValues) {
        this.validValues = validValues;
    }

    public boolean isAllowCustomValues() {
        return allowCustomValues;
    }

    public void setAllowCustomValues(boolean allowCustomValues) {
        this.allowCustomValues = allowCustomValues;
    }

    @Nullable
    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(@Nullable Integer minLength) {
        this.minLength = minLength;
    }

    @Nullable
    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(@Nullable Integer maxLength) {
        this.maxLength = maxLength;
    }

    public ValidationMode getMode() {
        return mode;
    }

    public void setMode(ValidationMode mode) {
        this.mode = mode;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }
}