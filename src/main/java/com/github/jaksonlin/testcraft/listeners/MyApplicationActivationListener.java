package com.github.jaksonlin.testcraft.listeners;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

public class MyApplicationActivationListener implements ApplicationActivationListener {

    private static final Logger log = Logger.getInstance(MyApplicationActivationListener.class);

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        log.warn("plugin activated");
    }
}
