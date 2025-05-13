package com.github.jaksonlin.testcraft.domain.annotations;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(typeName = "NullValue")
public class NullValue extends DefaultValue {
    public NullValue() {
        super("NullValue");
    }
} 