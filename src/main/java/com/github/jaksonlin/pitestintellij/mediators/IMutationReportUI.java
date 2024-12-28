package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.util.Pair;

import java.util.Map;

public interface IMutationReportUI {
    void updateMutationResult(String mutationClassFilePath, Map<Integer, Pair<String, Boolean>> mutationTestResult);
}