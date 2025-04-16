package com.github.jaksonlin.pitestintellij.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.jaksonlin.pitestintellij.MyBundle;
import javax.swing.*;

public class TestCraftSettingsConfigurable implements SearchableConfigurable.Parent {
    @Override
    public @NotNull String getId() {
        return "com.github.jaksonlin.pitestintellij.settings";
    }

    @Override
    public String getDisplayName() {
        return MyBundle.message("settings.testcraft.title");
    }

    @Override
    public @Nullable JComponent createComponent() {
        // Create a panel with a brief description of TestCraft settings
        JPanel panel = new JPanel();
        panel.add(new JLabel("<html><body style='width: 300px; padding: 10px;'>" +
                "<h2>" + MyBundle.message("settings.testcraft.title") + "</h2>" +
                "<p>" + MyBundle.message("settings.testcraft.description") + "</p>" +
                "<ul>" +
                "<li><b>" + MyBundle.message("settings.testcraft.annotations") + "</b> - " + MyBundle.message("settings.testcraft.annotations.description") + "</li>" +
                "<li><b>" + MyBundle.message("settings.testcraft.asserts") + "</b> - " + MyBundle.message("settings.testcraft.asserts.description") + "</li>" +
                "<li><b>" + MyBundle.message("settings.testcraft.llm") + "</b> - " + MyBundle.message("settings.testcraft.llm.description") + "</li>" +
                "</ul>" +
                "</body></html>"));
        return panel;
    }

    @Override
    public boolean hasOwnContent() {
        return true;
    }

    @Override
    public boolean isModified() {
        return false; // This is just a container, no settings to modify
    }

    @Override
    public void apply() {
        // No settings to apply in the parent
    }

    @Override
    public Configurable @NotNull [] getConfigurables() {
        return new Configurable[0]; // Child configurables are defined in plugin.xml
    }
}
