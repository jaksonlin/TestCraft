package com.github.jaksonlin.testcraft.context;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnittestCase {
    private final Map<String, Object> values;

    public UnittestCase(Map<String, Object> values) {
        this.values = values;
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value instanceof String ? (String) value : "";
    }

    public List<String> getStringList(String key) {
        Object value = values.get(key);
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            if (list.isEmpty() || list.get(0) instanceof String) {
                return (List<String>) value;
            }
        }
        return Collections.emptyList();
    }

    public String getStatus(String key) {
        Object value = values.get(key);
        return value instanceof String ? (String) value : "TODO";
    }

    public Map<String, Object> getValues() {
        return values;
    }
}