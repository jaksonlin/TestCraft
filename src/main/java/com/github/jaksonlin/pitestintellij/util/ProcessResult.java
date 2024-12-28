package com.github.jaksonlin.pitestintellij.util;

public class ProcessResult {
    private final int exitCode;
    private final String output;
    private final String errorOutput;

    public ProcessResult(int exitCode, String output, String errorOutput) {
        this.exitCode = exitCode;
        this.output = output;
        this.errorOutput = errorOutput;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorOutput() {
        return errorOutput;
    }
}
