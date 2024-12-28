package com.github.jaksonlin.pitestintellij.processor;

import com.github.jaksonlin.pitestintellij.context.TestPoints;
import com.intellij.ide.util.PropertiesComponent;

import java.util.Arrays;
import java.util.List;

public class TestPointProcessor {
    private static List<String> validTestPoints = TestPoints.builtinTestPoints; // Assuming builtinTestPoints is now a getter in TestPoints

    public static boolean isValidTestPoint(String testPoint) {
        return validTestPoints.contains(testPoint);
    }

    public static void loadCustomizedTestPoints() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        String defaultTestPoints = propertiesComponent.getValue(
                "defaultTestPoints",
                String.join(",", TestPoints.builtinTestPoints) // Assuming builtinTestPoints is now a getter in TestPoints
        );
        if (defaultTestPoints != null && !defaultTestPoints.isEmpty()) {
            validTestPoints = Arrays.asList(defaultTestPoints.split(","));
        }
    }

    public static List<String> getValidTestPoints() {
        return validTestPoints;
    }
}