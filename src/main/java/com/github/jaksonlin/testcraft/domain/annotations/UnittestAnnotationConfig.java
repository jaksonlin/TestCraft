package com.github.jaksonlin.testcraft.domain.annotations;

public class UnittestAnnotationConfig {
    private String authorField;
    private String titleField;
    private String targetClassField;
    private String targetMethodField;
    private String testPointsField;
    private String statusField;
    private String descriptionField;
    private String tagsField;
    private String relatedRequirementsField;
    private String relatedDefectsField;

    public UnittestAnnotationConfig() {
        this.authorField = "author";
        this.titleField = "title";
        this.targetClassField = "targetClass";
        this.targetMethodField = "targetMethod";
        this.testPointsField = "testPoints";
        this.statusField = "status";
        this.descriptionField = "description";
        this.tagsField = "tags";
        this.relatedRequirementsField = "relatedRequirements";
        this.relatedDefectsField = "relatedDefects";
    }

    public String getAuthorField() {
        return authorField;
    }

    public void setAuthorField(String authorField) {
        this.authorField = authorField;
    }

    public String getTitleField() {
        return titleField;
    }

    public void setTitleField(String titleField) {
        this.titleField = titleField;
    }

    public String getTargetClassField() {
        return targetClassField;
    }

    public void setTargetClassField(String targetClassField) {
        this.targetClassField = targetClassField;
    }

    public String getTargetMethodField() {
        return targetMethodField;
    }

    public void setTargetMethodField(String targetMethodField) {
        this.targetMethodField = targetMethodField;
    }

    public String getTestPointsField() {
        return testPointsField;
    }

    public void setTestPointsField(String testPointsField) {
        this.testPointsField = testPointsField;
    }

    public String getStatusField() {
        return statusField;
    }

    public void setStatusField(String statusField) {
        this.statusField = statusField;
    }

    public String getDescriptionField() {
        return descriptionField;
    }

    public void setDescriptionField(String descriptionField) {
        this.descriptionField = descriptionField;
    }

    public String getTagsField() {
        return tagsField;
    }

    public void setTagsField(String tagsField) {
        this.tagsField = tagsField;
    }

    public String getRelatedRequirementsField() {
        return relatedRequirementsField;
    }

    public void setRelatedRequirementsField(String relatedRequirementsField) {
        this.relatedRequirementsField = relatedRequirementsField;
    }

    public String getRelatedDefectsField() {
        return relatedDefectsField;
    }

    public void setRelatedDefectsField(String relatedDefectsField) {
        this.relatedDefectsField = relatedDefectsField;
    }
}