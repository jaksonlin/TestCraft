package com.github.jaksonlin.pitestintellij.calltracker;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

public class SootAnalyzer {

    public static void main(String[] args) {
        // Step 1: Set up Soot options
        String classToAnalyze = "com.analyzer.service.HelloService";  // Replace with the class you want to analyze

        // Step 2: Set up Soot
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_src_prec(Options.src_prec_class);
        String classpath = String.join(File.pathSeparator,
            "C:\\Users\\zhisl\\workspace\\jcalltrck\\build\\classes\\java\\main",
            "C:\\Users\\zhisl\\workspace\\utannfake\\lib\\build\\libs\\lib.jar"
           // javaHome + File.separator + "lib" + File.separator + "rt.jar"
        );
        Options.v().set_soot_classpath(classpath);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_verbose(true);
        Options.v().set_whole_program(true);


        // Verify Library class is loaded
        SootClass libraryClass = Scene.v().loadClassAndSupport("org.example.Library");
        if (libraryClass.isPhantom()) {
            System.err.println("Warning: org.example.Library is phantom - not properly loaded");
        }
        libraryClass.setLibraryClass();

        // Then proceed with loading and analyzing the target class
        SootClass sootClass = Scene.v().loadClassAndSupport(classToAnalyze);
        sootClass.setApplicationClass();

        // Step 4: Load necessary classes after setting up the main class
        Scene.v().loadNecessaryClasses();

        try {
            // Step 5: Iterate over all methods in the class
            for (SootMethod sootMethod : sootClass.getMethods())
            {
                System.out.println("Analyzing method: " + sootMethod.getName());
                
                // Use the CallTracker class to track the method calls
                CallTracker callTracker = new CallTracker(sootClass.getName(), sootMethod.getName(), "com.analyzer.model.Dummy");
                if (sootMethod.isConcrete()) {
                    try {
                        Body body = sootMethod.retrieveActiveBody();
                        for (Unit unit : body.getUnits()) {

                            analyzeUnit(unit, callTracker);
                        }
                    } catch (RuntimeException e) {
                        System.err.println("Error retrieving body for method: " + sootMethod.getName());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during analysis:");
            e.printStackTrace();
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

