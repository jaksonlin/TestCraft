package com.github.jaksonlin.pitestintellij.context;

import com.github.jaksonlin.pitestintellij.annotations.UnittestAnnotationConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnittestCaseInfoContext {
    private final Map<String, Object> annotationValues;
    private final UnittestAnnotationConfig config;
    private UnittestCaseStatus status = UnittestCaseStatus.TODO;
    private String description;
    private List<String> tags = Collections.emptyList();
    private List<String> relatedRequirements = Collections.emptyList();
    private List<String> relatedDefects = Collections.emptyList();

    public UnittestCaseInfoContext(Map<String, Object> annotationValues) {
        this(annotationValues, new UnittestAnnotationConfig());
    }

    public UnittestCaseInfoContext(Map<String, Object> annotationValues, UnittestAnnotationConfig config) {
        this.annotationValues = annotationValues;
        this.config = config;
        this.description = (String) annotationValues.getOrDefault(config.getDescriptionField(), "");
        this.tags = convertToStringList(annotationValues.get(config.getTagsField()));
        this.relatedRequirements = convertToStringList(annotationValues.get(config.getRelatedRequirementsField()));
        this.relatedDefects = convertToStringList(annotationValues.get(config.getRelatedDefectsField()));
    }

    public String getAuthor() {
        return (String) annotationValues.getOrDefault(config.getAuthorField(), "");
    }

    public String getTitle() {
        return (String) annotationValues.getOrDefault(config.getTitleField(), "");
    }

    public String getTargetClass() {
        return (String) annotationValues.getOrDefault(config.getTargetClassField(), "");
    }

    public String getTargetMethod() {
        return (String) annotationValues.getOrDefault(config.getTargetMethodField(), "");
    }

    public List<String> getTestPoints() {
        return convertToStringList(annotationValues.get(config.getTestPointsField()));
    }

    public UnittestCaseStatus getStatus() {
        return status;
    }

    public void setStatus(UnittestCaseStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getRelatedRequirements() {
        return relatedRequirements;
    }

    public void setRelatedRequirements(List<String> relatedRequirements) {
        this.relatedRequirements = relatedRequirements;
    }

    public List<String> getRelatedDefects() {
        return relatedDefects;
    }

    public void setRelatedDefects(List<String> relatedDefects) {
        this.relatedDefects = relatedDefects;
    }

    public Map<String, Object> getAnnotationValues() {
        return annotationValues;
    }

    public UnittestAnnotationConfig getConfig() {
        return config;
    }

    @SuppressWarnings("unchecked")
    private List<String> convertToStringList(Object value) {
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}