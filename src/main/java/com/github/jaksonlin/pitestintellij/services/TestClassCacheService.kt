package com.github.jaksonlin.pitestintellij.services

import com.intellij.openapi.components.Service
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class TestClassCacheService {
    private val testClassCache = ConcurrentHashMap<String, Boolean>()

    fun isTestClass(qualifiedName: String, compute: () -> Boolean): Boolean {
        return testClassCache.getOrPut(qualifiedName, compute)
    }

    fun clearCache() {
        testClassCache.clear()
    }
}