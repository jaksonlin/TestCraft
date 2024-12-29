package com.github.jaksonlin.pitestintellij.mediators;

public interface IMutationMediator {
    void processMutationResult(String mutationTargetClassFilePath, String mutationReportFilePath);
    void register(IMutationReportUI clientUI);
}
