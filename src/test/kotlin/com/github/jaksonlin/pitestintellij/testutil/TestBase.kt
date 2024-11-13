package com.github.jaksonlin.pitestintellij.testutil

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.intellij.openapi.Disposable
import com.intellij.testFramework.replaceService

interface TestBase {
    // Remove the property requirement and use a parameter instead
    fun setupServices(disposable: Disposable) {
        val application = ApplicationManager.getApplication()
        application.replaceService(
            AnnotationConfigService::class.java, 
            AnnotationConfigService(), 
            disposable
        )
    }
}