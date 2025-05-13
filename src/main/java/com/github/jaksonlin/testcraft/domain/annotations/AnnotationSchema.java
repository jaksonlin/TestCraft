package com.github.jaksonlin.testcraft.domain.annotations;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JSONType(typeName = "AnnotationSchema")
public class AnnotationSchema {
    @JSONField(name = "annotationClassName")
    private String annotationClassName;
    private List<AnnotationFieldConfig> fields;
    private static final String DEFAULT_SCHEMA_PATH = "/schemas/default_annotation_schema.json";

 
    public AnnotationSchema() {
    }

    public AnnotationSchema(String annotationClassName, List<AnnotationFieldConfig> fields) {
        this.annotationClassName = annotationClassName;
        this.fields = fields;
    }

    public String getAnnotationClassName() {
        return annotationClassName;
    }

    public void setAnnotationClassName(String annotationClassName) {
        this.annotationClassName = annotationClassName;
    }

    public List<AnnotationFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<AnnotationFieldConfig> fields) {
        this.fields = fields;
    }

    public static String getDefaultSchema(){
        try (InputStream is = AnnotationSchema.class.getResourceAsStream(DEFAULT_SCHEMA_PATH)) {
            if (is == null) {
                throw new RuntimeException("Could not find default schema file: " + DEFAULT_SCHEMA_PATH);
            }
           return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default schema", e);
        }
    }

    public static class Companion {
        private static final JSON json;

        static {
            String schemaContent = getDefaultSchema();
            json = JSON.parseObject(schemaContent);
        }

        @Nullable
        public static AnnotationSchema fromJson(@NotNull String jsonString) {
            try {
                return JSON.parseObject(jsonString, AnnotationSchema.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}