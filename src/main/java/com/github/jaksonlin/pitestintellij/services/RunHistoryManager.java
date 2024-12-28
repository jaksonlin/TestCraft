package com.github.jaksonlin.pitestintellij.services;

import com.github.jaksonlin.pitestintellij.context.PitestContext;
import com.github.jaksonlin.pitestintellij.observers.ObserverBase;
import com.github.jaksonlin.pitestintellij.observers.RunHistoryObserver;
import com.github.jaksonlin.pitestintellij.util.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class RunHistoryManager extends ObserverBase {
    private final Project project;
    private final Gson gson = new Gson();
    private final File historyFile;
    private final Map<String, PitestContext> history;

    public RunHistoryManager(@NotNull Project project) {
        this.project = project;
        this.historyFile = new File(PathManager.getConfigPath(), "run-" + project.getName() + "-history.json");
        this.history = loadRunHistory();
    }

    @Override
    public void addObserver(RunHistoryObserver observer) {
        super.addObserver(observer);
        // pass current value of history to observer, List<Pair<String, String>>
        List<Pair<String, String>> mappedHistory = history.entrySet().stream()
                .map(entry -> new Pair<>(entry.getValue().getTargetClassPackageName(), entry.getValue().getTargetClassName()))
                .collect(Collectors.toList());
        observer.onRunHistoryChanged(mappedHistory);
    }

    @Nullable
    public PitestContext getRunHistoryForClass(@NotNull String targetClassFullyQualifiedName) {
        return history.get(targetClassFullyQualifiedName);
    }

    public void clearRunHistory() {
        history.clear();
        if (historyFile.exists()) {
            historyFile.delete();
        }
        notifyObservers(null);
    }

    @NotNull
    public Map<String, PitestContext> getRunHistory() {
        return new HashMap<>(history);
    }

    public void saveRunHistory(@NotNull PitestContext entry) {
        history.put(entry.getTargetClassFullyQualifiedName(), entry);
        try {
            String json = gson.toJson(history);
            Files.write(historyFile.toPath(), json.getBytes());
            // this should be a Pair<String, String>>
            notifyObservers(new Pair<String, String>(entry.getTargetClassPackageName(), entry.getTargetClassName()));
        } catch (IOException e) {
            // Handle the exception appropriately, e.g., log an error
            e.printStackTrace();
        }
    }

    @NotNull
    private Map<String, PitestContext> loadRunHistory() {
        if (!historyFile.exists()) {
            return new HashMap<>();
        }
        try {
            byte[] bytes = Files.readAllBytes(historyFile.toPath());
            String json = new String(bytes);
            Type type = new TypeToken<HashMap<String, PitestContext>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (IOException | JsonSyntaxException e) {
            if (historyFile.exists()) {
                historyFile.delete();
            }
            return new HashMap<>();
        }
    }
}
