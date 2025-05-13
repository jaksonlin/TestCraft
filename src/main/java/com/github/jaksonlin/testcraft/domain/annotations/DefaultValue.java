package com.github.jaksonlin.testcraft.domain.annotations;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.List;

@JSONType(typeName = "DefaultValue", seeAlso = {
    StringValue.class,
    StringListValue.class,
    NullValue.class
})
public abstract class DefaultValue {
    @JSONField(name = "type")
    private String type;

    public DefaultValue() {
    }

    public DefaultValue(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JSONType(typeName = "StringValue")
    public static class StringValue extends DefaultValue {
        @JSONField(name = "value")
        private String value;

        public StringValue() {
            super("StringValue");
        }

        public StringValue(String value) {
            super("StringValue");
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @JSONType(typeName = "StringListValue")
    public static class StringListValue extends DefaultValue {
        @JSONField(name = "value")
        private List<String> value;

        public StringListValue() {
            super("StringListValue");
        }

        public StringListValue(List<String> value) {
            super("StringListValue");
            this.value = value;
        }

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }
    }

    @JSONType(typeName = "NullValue")
    public static class NullValue extends DefaultValue {
        public NullValue() {
            super("NullValue");
        }
    }
}
