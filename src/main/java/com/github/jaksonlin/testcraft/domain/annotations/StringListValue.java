package com.github.jaksonlin.testcraft.domain.annotations;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.List;

@JSONType(typeName = "StringListValue")
public class StringListValue extends DefaultValue {
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