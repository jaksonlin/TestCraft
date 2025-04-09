package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "com.github.jaksonlin.pitestintellij.settings.OllamaSettingsState",
    storages = @Storage("OllamaSettings.xml")
)
public class OllamaSettingsState implements PersistentStateComponent<OllamaSettingsState> {
    public String ollamaHost = "localhost";
    public int ollamaPort = 11434;
    public String ollamaModel = "deepseek-r1:32b";
    public int maxTokens = 2000;
    public float temperature = 0.7f;
    public int requestTimeout = 60;  // seconds

    public static OllamaSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(OllamaSettingsState.class);
    }

    @Nullable
    @Override
    public OllamaSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull OllamaSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
} 