package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.util.Mutation;

import java.util.List;

public interface IMutationMediator {
    void processMutationResult(String mutationTargetClassFilePath, List<Mutation> mutationList);
    void register(IMutationReportUI clientUI);
}
