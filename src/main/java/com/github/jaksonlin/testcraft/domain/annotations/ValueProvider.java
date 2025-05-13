package com.github.jaksonlin.testcraft.domain.annotations;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.List;

public class ValueProvider {
    @JSONField(name = "type")
    private ValueProviderType type;

    @JSONField(name = "format", defaultValue = "")
    private String format;

    @JSONField(name = "value", defaultValue = "")
    private String value;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

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

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value; 
    }
    

   
}

