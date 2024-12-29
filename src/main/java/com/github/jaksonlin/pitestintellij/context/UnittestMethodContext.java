package com.github.jaksonlin.pitestintellij.context;

import java.util.List;

public class UnittestMethodContext {

    private final String methodName;
    private final List<String> comments;

    public UnittestMethodContext(String methodName, List<String> comments) {
        this.methodName = methodName;
        this.comments = comments;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getComments() {
        return comments;
    }
}