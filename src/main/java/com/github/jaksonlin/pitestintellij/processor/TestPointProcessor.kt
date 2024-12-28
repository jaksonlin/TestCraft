package com.github.jaksonlin.pitestintellij.processor

import com.github.jaksonlin.pitestintellij.context.TestPoints
import com.intellij.ide.util.PropertiesComponent


class TestPointProcessor {
    companion object {
        private var validTestPoints = TestPoints.builtinTestPoints

        fun isValidTestPoint(testPoint: String): Boolean = validTestPoints.contains(testPoint)

        fun loadCustomizedTestPoints() {
            val propertiesComponent = PropertiesComponent.getInstance()
            val defaultTestPoints = propertiesComponent.getValue(
                "defaultTestPoints",
                TestPoints.builtinTestPoints.joinToString(",")
            )
            validTestPoints = defaultTestPoints.split(",")
        }

        fun getValidTestPoints(): List<String> = validTestPoints
    }
}