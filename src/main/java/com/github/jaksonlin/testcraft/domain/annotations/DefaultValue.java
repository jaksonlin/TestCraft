package com.github.jaksonlin.testcraft.domain.annotations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultValue.StringValue.class, name = "StringValue"),
        @JsonSubTypes.Type(value = DefaultValue.StringListValue.class, name = "StringListValue"),
        @JsonSubTypes.Type(value = DefaultValue.NullValue.class, name = "NullValue")
})
public abstract class DefaultValue {

    public static class StringValue extends DefaultValue {
        private String value;

        public StringValue() {
        }

        public StringValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class StringListValue extends DefaultValue {
        private List<String> value;

        public StringListValue() {
        }

        public StringListValue(List<String> value) {
            this.value = value;
        }

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }
    }

    public static class NullValue extends DefaultValue {
        // No fields needed for NullValue
    }
}
