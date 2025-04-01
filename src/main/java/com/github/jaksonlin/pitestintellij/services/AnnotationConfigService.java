package com.github.jaksonlin.pitestintellij.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

@Service(Service.Level.APP)
@State(
        name = "AnnotationConfig",
        storages = {@Storage("pitestAnnotationConfig.xml")}
)
public final class AnnotationConfigService implements PersistentStateComponent<AnnotationConfigService.State> {
    private static final Logger LOG = Logger.getInstance(AnnotationConfigService.class);
    private static final ObjectMapper jsonMapper = JsonMapper.builder().build();

    public static class State {
        public String schemaJson = AnnotationSchema.DEFAULT_SCHEMA;
        public String annotationPackage = "com.example.unittest.annotations";
        public boolean shouldCheckAnnotation = false;
        public boolean autoImport = true;

        public State() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return autoImport == state.autoImport &&
                    Objects.equals(schemaJson, state.schemaJson) &&
                    Objects.equals(annotationPackage, state.annotationPackage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(schemaJson, annotationPackage, autoImport);
        }
    }

    private State myState = new State();

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        LOG.info("Loading annotation config: " + state.schemaJson);
        myState = state;
    }

    public AnnotationSchema getSchema() {
        try {
            return jsonMapper.readValue(myState.schemaJson, AnnotationSchema.class);
        } catch (IOException e) {
            try {
                return jsonMapper.readValue(AnnotationSchema.DEFAULT_SCHEMA, AnnotationSchema.class);
            } catch (IOException ex) {
                // Should not happen, but handle for robustness
                LOG.error("Error decoding default schema", ex);
                return null; // Or throw an exception depending on your error handling strategy
            }
        }
    }

    public void updateSchema(AnnotationSchema schema) {
        try {
            myState.schemaJson = jsonMapper.writeValueAsString(schema);
            LOG.info("Updated annotation config: " + myState.schemaJson);
        } catch (IOException e) {
            LOG.error("Error encoding schema", e);
        }
    }

    public AnnotationSchema getBuildInSchema() {
        try {
            return jsonMapper.readValue(AnnotationSchema.DEFAULT_SCHEMA, AnnotationSchema.class);
        } catch (IOException e) {
            LOG.error("Error decoding default schema", e);
            return null; // Or throw an exception
        }
    }

    public String getAnnotationPackage() {
        return myState.annotationPackage;
    }

    public void setAnnotationPackage(String packageName) {
        myState.annotationPackage = packageName;
        LOG.info("Updated annotation package: " + packageName);
    }

    public boolean isAutoImport() {
        return myState.autoImport;
    }

    public void setAutoImport(boolean auto) {
        myState.autoImport = auto;
        LOG.info("Updated auto import setting: " + auto);
    }

    public boolean shouldCheckAnnotation() {
        return myState.shouldCheckAnnotation;
    }

    public void setShouldCheckAnnotation(boolean shouldCheckAnnotation) {
        myState.shouldCheckAnnotation = shouldCheckAnnotation;
        LOG.info("Updated should check annotation setting: " + shouldCheckAnnotation);
    }
}
