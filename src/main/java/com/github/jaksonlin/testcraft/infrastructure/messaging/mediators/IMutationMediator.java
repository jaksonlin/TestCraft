package com.github.jaksonlin.testcraft.infrastructure.messaging.mediators;

import com.github.jaksonlin.testcraft.util.Mutation;

import java.util.List;

public interface IMutationMediator {
    void processMutationResult(String mutationTargetClassFilePath, List<Mutation> mutationList);
}
