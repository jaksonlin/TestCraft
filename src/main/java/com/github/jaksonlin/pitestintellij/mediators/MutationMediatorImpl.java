package com.github.jaksonlin.pitestintellij.mediators;

import com.github.jaksonlin.pitestintellij.util.Mutation;
import com.github.jaksonlin.pitestintellij.util.MutationReportParser;
import com.github.jaksonlin.pitestintellij.util.Pair;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MutationMediatorImpl implements IMutationMediator {
    private IMutationReportUI clientUI;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void processMutationResult(String mutationTargetClassFilePath, String mutationReportFilePath) {
        executorService.submit(() -> {
            List<Mutation> mutations = null;
            try {
                mutations = MutationReportParser.parseMutationsFromXml(mutationReportFilePath).getMutation();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Map<Integer, Pair<String, Boolean>> renderedFormat = convertResultToUIRenderFormat(mutations);
        if (clientUI != null) {
            SwingUtilities.invokeLater(() -> clientUI.updateMutationResult(mutationTargetClassFilePath, renderedFormat));
        }
    });
    }

    private Map<Integer, Pair<String, Boolean>> convertResultToUIRenderFormat(List<Mutation> mutations) {
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

    @Override
    public void register(IMutationReportUI clientUI) {
        this.clientUI = clientUI;
    }


}
