package com.github.jaksonlin.testcraft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class ProcessExecutor {
    public static ProcessResult executeProcess(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.environment().put("GRADLE_OPTS", "-Dorg.gradle.daemon=false -Dorg.gradle.debug=true");
            Process process = builder.start();
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            StringBuilder debugInfo = new StringBuilder();
            
            debugInfo.append("Process execution details:\n")
                    .append("Command: ").append(String.join(" ", command)).append("\n")
                    .append("Working directory: ").append(builder.directory()).append("\n")
                    .append("Environment: ").append(builder.environment()).append("\n");

            try (
                    BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))
            ) {
                Thread outputThread = new Thread(() -> outputReader.lines().forEach(line -> output.append(line).append(System.lineSeparator())));
                Thread errorThread = new Thread(() -> errorReader.lines().forEach(line -> errorOutput.append(line).append(System.lineSeparator())));

                outputThread.start();
                errorThread.start();

                int exitCode = process.waitFor();
                outputThread.join();
                errorThread.join();

                debugInfo.append("Exit code: ").append(exitCode).append("\n");
                if (exitCode != 0) {
                    debugInfo.append("Process terminated abnormally\n");
                    try {
                        debugInfo.append("Process info:\n")
                                .append("Is alive: ").append(process.isAlive()).append("\n")
                                .append("Total memory: ").append(Runtime.getRuntime().totalMemory()).append("\n")
                                .append("Free memory: ").append(Runtime.getRuntime().freeMemory()).append("\n");
                    } catch (Exception e) {
                        debugInfo.append("Could not get process info: ").append(e.getMessage()).append("\n");
                    }
                }

                return new ProcessResult(exitCode, output.toString(), 
                    errorOutput.toString() + "\n=== Debug Information ===\n" + debugInfo.toString());
            }
        } catch (IOException | InterruptedException e) {
            return new ProcessResult(-1, "", 
                "Process execution failed: " + e.getMessage() + "\n" + 
                "Stack trace: " + getStackTraceAsString(e));
        }
    }

    private static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}