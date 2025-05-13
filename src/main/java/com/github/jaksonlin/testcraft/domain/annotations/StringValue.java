package com.github.jaksonlin.testcraft.domain.annotations;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "StringValue")
public class StringValue extends DefaultValue {
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