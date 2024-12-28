package com.github.jaksonlin.pitestintellij;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class MyBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.MyBundle";
    private static final MyBundle INSTANCE = new MyBundle();

    private MyBundle() {
        super(BUNDLE);
    }

    public static @NotNull String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static @NotNull Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }

    public static MyBundle getInstance() {
        return INSTANCE;
    }
}