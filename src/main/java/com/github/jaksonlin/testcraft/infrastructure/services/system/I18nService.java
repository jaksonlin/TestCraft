package com.github.jaksonlin.testcraft.infrastructure.services.system;

import java.util.ResourceBundle;

import org.jetbrains.annotations.PropertyKey;

import com.intellij.AbstractBundle;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.application.ApplicationManager;

@Service(Service.Level.APP)
public class I18nService {
    private final String BUNDLE = "messages.MyBundle";
    private ResourceBundle ourBundle;
    public String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return AbstractBundle.message(getBundle(), key, params);
    }
    private ResourceBundle getBundle() {
        if (ourBundle == null) {
            ourBundle = ResourceBundle.getBundle(BUNDLE);
        }
        return ourBundle;
    }

    public static I18nService getInstance() {
        return ApplicationManager.getApplication().getService(I18nService.class);
    }
}
