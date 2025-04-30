package com.github.jaksonlin.testcraft.core.services;

import com.github.jaksonlin.testcraft.core.context.PitestContext;
import com.github.jaksonlin.testcraft.messaging.observers.ObserverBase;
import com.github.jaksonlin.testcraft.messaging.observers.BasicEventObserver;
import com.github.jaksonlin.testcraft.util.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class RunHistoryManagerService extends ObserverBase {
    private static final Logger log = LoggerFactory.getLogger(RunHistoryManagerService.class);
    private final Project project;
    private final Gson gson = new Gson();
    private final File historyFile;
    private final Map<String, PitestContext> history;

    public RunHistoryManagerService(@NotNull Project project) {
        this.project = project;
        this.historyFile = new File(PathManager.getConfigPath(), "run-" + project.getName() + "-history.json");
        this.history = loadRunHistory();
    }

    @Override
    public void addObserver(BasicEventObserver observer) {
        super.addObserver(observer);
        // when observer is added, pass current value of history to observer, force it to update
        // pass current value of history to observer, List<Pair<String, String>>
        List<Pair<String, String>> mappedHistory = history.entrySet().stream()
                .map(entry -> new Pair<>(entry.getValue().getTargetClassPackageName(), entry.getValue().getTargetClassName()))
                .collect(Collectors.toList());
        observer.onEventHappen("RUN_HISTORY", mappedHistory);
        observer.onEventHappen("RUN_HISTORY_LIST", getRunHistory());
    }

    @Nullable
    public PitestContext getRunHistoryForClass(@NotNull String targetClassFullyQualifiedName) {
        return history.get(targetClassFullyQualifiedName);
    }

    @Nullable
    public PitestContext getRunHistoryForClassByTargetFilePath(@NotNull String classUnderTestFilePath) {
        for (PitestContext context : history.values()) {
            if (context.getTargetClassFilePath().equals(classUnderTestFilePath)) {
                return context;
            }
        }
        return null;
    }

    public void clearRunHistory() {
        history.clear();
        if (historyFile.exists()) {
            historyFile.delete();
        }
        notifyObservers("RUN_HISTORY", null);
        notifyObservers("RUN_HISTORY_LIST", null);
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
            notifyObservers("RUN_HISTORY", new Pair<String, String>(entry.getTargetClassPackageName(), entry.getTargetClassName()));
            notifyObservers("RUN_HISTORY_LIST", getRunHistory());
        } catch (IOException e) {
            // Handle the exception appropriately, e.g., log an error
            log.error("Error saving run history", e);
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
