package com.github.jaksonlin.pitestintellij.calltracker;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.options.Options;

import java.util.ArrayList;
import java.util.List;

public class SootAnalyzer {

    public static void main(String[] args) {
        // Step 1: Set up Soot options
        String classToAnalyze = "com.analyzer.service.HelloService";  // Replace with the class you want to analyze

        // Step 2: Set up Soot
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath("C:\\Users\\zhisl\\workspace\\jcalltrck\\target\\classes");
        // Step 3: Load the class to analyze
        SootClass sc = Scene.v().loadClassAndSupport(classToAnalyze);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        // Step 4: Start the analysis
        analyzeMethods(classToAnalyze);
    }

    private static void analyzeMethods(String className) {
        // Step 5: Analyze methods in the specified class
        SootClass sootClass = Scene.v().loadClassAndSupport(className);

        // Step 6: Iterate over all methods in the class
        for (SootMethod sootMethod : sootClass.getMethods()) {
            System.out.println("Analyzing method: " + sootMethod.getName());

            // Use the CallTracker class to track the method calls
            CallTracker callTracker = new CallTracker(sootClass.getName(), sootMethod.getName(), "com.analyzer.model.Dummy");
            if (sootMethod.isConcrete()) {
                try {
                    Body body = sootMethod.retrieveActiveBody();
                    for (Unit unit : body.getUnits()) {
                        // Perform the analysis of each statement and method calls
                        analyzeUnit(unit, callTracker);
                    }
                } catch (RuntimeException e) {
                    System.err.println("Error retrieving body for method: " + sootMethod.getName());
                    e.printStackTrace();
                }
            }
        }
    }
    private static void analyzeUnit(Unit unit, CallTracker callTracker) {
        // Analyze each statement or method call in the unit (e.g., method calls, assignments)
        if (unit instanceof InvokeStmt invokeStmt) {
            analyzeInvokeExpr(invokeStmt.getInvokeExpr(), callTracker);
        } else if (unit instanceof AssignStmt assignStmt) {
            Value rightOp = assignStmt.getRightOp();
            if (rightOp instanceof InvokeExpr invokeExpr) {
                analyzeInvokeExpr(invokeExpr, callTracker);
            }
        }
    }

    private static void analyzeInvokeExpr(InvokeExpr invokeExpr, CallTracker callTracker) {
        // Extract method call details (class, method name, arguments)
        String calledClassName = invokeExpr.getMethod().getDeclaringClass().getName();
        String calledMethodName = invokeExpr.getMethod().getName();
        String methodDescriptor = invokeExpr.getMethod().getSubSignature();
        List<String> arguments = new ArrayList<>();

        for (Value arg : invokeExpr.getArgs()) {
            // Extract argument type (or use other information if necessary)
            arguments.add(arg.getType().toString());
        }

        // Add the method call details to the tracker
        callTracker.addMethodCall(calledClassName, calledMethodName, methodDescriptor, arguments);

        // Recursively analyze arguments if they are also method calls
        for (Value arg : invokeExpr.getArgs()) {
            if (arg instanceof InvokeExpr nestedInvokeExpr) {
                analyzeInvokeExpr(nestedInvokeExpr, callTracker);
            }
        }
    }
}

