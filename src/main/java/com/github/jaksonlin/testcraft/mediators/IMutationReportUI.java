package com.github.jaksonlin.testcraft.mediators;

import com.github.jaksonlin.testcraft.util.Pair;

import java.util.Map;

public interface IMutationReportUI {
    void updateMutationResult(String mutationClassFilePath, Map<Integer, Pair<String, Boolean>> mutationTestResult);
}