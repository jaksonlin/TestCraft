package com.github.jaksonlin.testcraft.util;

public class Mutation {
    private boolean detected;
    private String status;
    private int numberOfTestsRun;
    private String sourceFile;
    private String mutatedClass;
    private String mutatedMethod;
    private String methodDescription;
    private int lineNumber;
    private String mutator;
    private Indexes indexes;
    private Blocks blocks;
    private String killingTest;
    private String description;

    public Mutation() {
    }

    public Mutation(boolean detected, String status, int numberOfTestsRun, String sourceFile, String mutatedClass, String mutatedMethod, String methodDescription, int lineNumber, String mutator, Indexes indexes, Blocks blocks, String killingTest, String description) {
        this.detected = detected;
        this.status = status;
        this.numberOfTestsRun = numberOfTestsRun;
        this.sourceFile = sourceFile;
        this.mutatedClass = mutatedClass;
        this.mutatedMethod = mutatedMethod;
        this.methodDescription = methodDescription;
        this.lineNumber = lineNumber;
        this.mutator = mutator;
        this.indexes = indexes;
        this.blocks = blocks;
        this.killingTest = killingTest;
        this.description = description;
    }

    public boolean isDetected() {
        return detected;
    }

    public String getStatus() {
        return status;
    }

    public int getNumberOfTestsRun() {
        return numberOfTestsRun;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getMutatedClass() {
        return mutatedClass;
    }

    public String getMutatedMethod() {
        return mutatedMethod;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMutator() {
        return mutator;
    }

    public Indexes getIndexes() {
        return indexes;
    }

    public Blocks getBlocks() {
        return blocks;
    }

    public String getKillingTest() {
        return killingTest;
    }

    public String getDescription() {
        return description;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNumberOfTestsRun(int numberOfTestsRun) {
        this.numberOfTestsRun = numberOfTestsRun;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setMutatedClass(String mutatedClass) {
        this.mutatedClass = mutatedClass;
    }

    public void setMutatedMethod(String mutatedMethod) {
        this.mutatedMethod = mutatedMethod;
    }

    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setMutator(String mutator) {
        this.mutator = mutator;
    }

    public void setIndexes(Indexes indexes) {
        this.indexes = indexes;
    }

    public void setBlocks(Blocks blocks) {
        this.blocks = blocks;
    }

    public void setKillingTest(String killingTest) {
        this.killingTest = killingTest;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
