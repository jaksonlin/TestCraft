package com.github.jaksonlin.testcraft.annotations;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;

public class ValueProvider {
    private ValueProviderType type;
    @Nullable
    private String format;
    @Nullable
    private JsonNode value;

    public ValueProvider() {
    }

    public ValueProvider(ValueProviderType type) {
        this.type = type;
    }

    public ValueProviderType getType() {
        return type;
    }

    public void setType(ValueProviderType type) {
        this.type = type;
    }

    @Nullable
    public String getFormat() {
        return format;
    }

    public void setFormat(@Nullable String format) {
        this.format = format;
    }

    @Nullable
    public JsonNode getValue() {
        return value;
    }

    public void setValue(@Nullable JsonNode value) {
        this.value = value;
    }
}

