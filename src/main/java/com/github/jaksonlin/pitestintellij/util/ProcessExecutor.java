package com.github.jaksonlin.pitestintellij.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ProcessExecutor {
    public static ProcessResult executeProcess(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

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

                return new ProcessResult(exitCode, output.toString(), errorOutput.toString());
            }
        } catch (IOException | InterruptedException e) {
            return new ProcessResult(-1, "", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
        }
    }
}