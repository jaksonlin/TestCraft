package com.github.jaksonlin.testcraft.util;

import java.util.List;
public class ClassFileInfo {
    private final String fullyQualifiedName;
    private final String className;
    private final String packageName;
    private final List<String> methods;

    public ClassFileInfo(String fullyQualifiedName, String className, String packageName, List<String> methods) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.className = className;
        this.packageName = packageName;
        this.methods = methods;
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

    public List<String> getMethods() {
        return methods;
    }
}
