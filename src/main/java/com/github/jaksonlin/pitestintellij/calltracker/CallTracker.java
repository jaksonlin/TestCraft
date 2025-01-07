package com.github.jaksonlin.pitestintellij.calltracker;

/**
 * This class it used to track the call of a method for the purpose of reverse engineering the building up of the dependent data that unit test needs.
 */

import java.util.List;
import java.util.ArrayList;

/**
 * This class is used to track the call of a method for the purpose of reverse engineering the building up of the dependent data that unit tests need.
 */
public class CallTracker {

    // Class and method to analyze
    private final String className;
    private final String methodName;

    // The type of parameter to track (e.g., class name, type)
    private final String variableTypeToTrack;

    // List of method calls that involve the tracked parameter type
    private final List<MethodCall> methodCalls;

    // List of external dependencies (JARs, libraries)
    private final List<String> externalDependencies;

    public CallTracker(String className, String methodName, String variableTypeToTrack) {
        this.className = className;
        this.methodName = methodName;
        this.variableTypeToTrack = variableTypeToTrack;
        this.methodCalls = new ArrayList<>();
        this.externalDependencies = new ArrayList<>();
    }

    // Add a method call (tracking the method, arguments, and return type)
    public void addMethodCall(String calledClassName, String calledMethodName, String calledMethodDescriptor, List<String> arguments) {
        this.methodCalls.add(new MethodCall(calledClassName, calledMethodName, calledMethodDescriptor, arguments));
    }

    // Add an external dependency (JAR or library)
    public void addExternalDependency(String dependency) {
        this.externalDependencies.add(dependency);
    }

    // Getters for the tracked information
    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getVariableTypeToTrack() {
        return variableTypeToTrack;
    }

    public List<MethodCall> getMethodCalls() {
        return methodCalls;
    }

    public List<String> getExternalDependencies() {
        return externalDependencies;
    }

    // Inner class representing a method call
    public static class MethodCall {
        private final String className;
        private final String methodName;
        private final String methodDescriptor;  // e.g., (Ljava/lang/String;)V
        private final List<String> arguments;   // List of argument types passed to the method

        public MethodCall(String className, String methodName, String methodDescriptor, List<String> arguments) {
            this.className = className;
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
            this.arguments = arguments;
        }

        // Getters for method call details
        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getMethodDescriptor() {
            return methodDescriptor;
        }

        public List<String> getArguments() {
            return arguments;
        }
    }
}
