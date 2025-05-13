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


}
