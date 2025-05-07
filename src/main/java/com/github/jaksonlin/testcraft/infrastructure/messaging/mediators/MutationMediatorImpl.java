package com.github.jaksonlin.testcraft.infrastructure.messaging.mediators;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.MutationEvent;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.github.jaksonlin.testcraft.util.Mutation;
import com.github.jaksonlin.testcraft.util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MutationMediatorImpl implements IMutationMediator {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final EventBusService eventBusService = EventBusService.getInstance();

    @Override
    public void processMutationResult(String mutationTargetClassFilePath, List<Mutation> mutations) {
        executorService.submit(() -> {
            Map<Integer, Pair<String, Boolean>> renderedFormat = convertResultToUIRenderFormat(mutations);
            eventBusService.post(new MutationEvent(MutationEvent.MUTATION_RESULT, new Pair<String, Map<Integer, Pair<String, Boolean>>>(mutationTargetClassFilePath, renderedFormat)));
        });
    }

    protected Map<Integer, Pair<String, Boolean>> convertResultToUIRenderFormat(List<Mutation> mutations) {
        Map<Integer, List<Pair<String, Boolean>>> groupedResult = new HashMap<>();

        for (Mutation mutation : mutations) {
            int line = mutation.getLineNumber();
            String mutationMessage = mutationMessageFormat(mutation, groupedResult.containsKey(line) ? groupedResult.get(line).size() + 1 : 1);
            Pair<String, Boolean> mutationPair = new Pair<>(mutationMessage, mutation.getStatus().equals("KILLED"));

            if (groupedResult.containsKey(line)) {
                List<Pair<String, Boolean>> existingList = groupedResult.get(line);
                existingList.add(mutationPair);
            } else {
                List<Pair<String, Boolean>> newList = new ArrayList<>();
                newList.add(mutationPair);
                groupedResult.put(line, newList);
            }
        }

        Map<Integer, Pair<String, Boolean>> finalResult = new HashMap<>();
        for (Map.Entry<Integer, List<Pair<String, Boolean>>> entry : groupedResult.entrySet()) {
            boolean isAllKilled = true;
            StringBuilder messageBuilder = new StringBuilder();
            for (Pair<String, Boolean> pair : entry.getValue()) {
                if (!pair.getSecond()) {
                    isAllKilled = false;
                }
                messageBuilder.append(pair.getFirst()).append("\n");
            }
            finalResult.put(entry.getKey(), new Pair<>(messageBuilder.toString().trim(), isAllKilled));
        }
        return finalResult;
    }

    private String mutationMessageFormat(Mutation mutation, int groupNumber) {
        return groupNumber + " " + mutation.getDescription() + " -> " + mutation.getStatus();
    }


}
