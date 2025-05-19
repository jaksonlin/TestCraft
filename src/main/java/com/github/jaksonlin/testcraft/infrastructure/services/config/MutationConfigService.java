package com.github.jaksonlin.testcraft.infrastructure.services.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.application.ApplicationManager;
import java.util.Objects;

@Service(Service.Level.APP)
@State(
    name = "com.github.jaksonlin.testcraft.infrastructure.services.MutationSettings",
    storages = @Storage(value = "$APP_CONFIG$/MutationSettings.xml")
)
public final class MutationConfigService implements PersistentStateComponent<MutationConfigService.State> {
    

    public static MutationConfigService getInstance() {
        return ApplicationManager.getApplication().getService(MutationConfigService.class);
    }

    public static class State {
        public String mutatorGroup = "STARTER_KIT"; // Default value

        public State() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return mutatorGroup.equals(state.mutatorGroup);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mutatorGroup);
        }
    }

    private State myState = new State();

    public String getMutatorGroup() {
        return myState.mutatorGroup;
    }

    public void setMutatorGroup(String mutatorGroup) {
        myState.mutatorGroup = mutatorGroup;
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

   
} 