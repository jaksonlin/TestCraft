package com.github.jaksonlin.pitestintellij.util;

public class ClassFileInfo {
    private final String fullyQualifiedName;
    private final String className;
    private final String packageName;

    public ClassFileInfo(String fullyQualifiedName, String className, String packageName) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.className = className;
        this.packageName = packageName;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }
}
