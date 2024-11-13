package com.github.jaksonlin.pitestintellij.inspectors

import com.github.jaksonlin.pitestintellij.commands.casecheck.CheckAnnotationCommand
import com.github.jaksonlin.pitestintellij.commands.casecheck.UnittestFileInspectorCommand
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.concurrent.ConcurrentHashMap

class UnittestInspector : AbstractBaseJavaLocalInspectionTool() {
    // Cache test annotation qualified names for faster lookup
    private val testAnnotations = setOf(
        "org.junit.Test",
        "org.junit.jupiter.api.Test",
        "Test"
    )

    private val testClassAnnotations = setOf(
        "org.junit.runner.RunWith",
        "org.junit.jupiter.api.TestInstance",
        "org.junit.platform.suite.api.Suite"
    )

    // Cache test class status to avoid repeated checks
    private val testClassCache = ConcurrentHashMap<String, Boolean>()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        return object : JavaElementVisitor() {
            override fun visitMethod(psiMethod: PsiMethod) {
                // Quick check for test annotation before doing anything else
                if (!hasTestAnnotation(psiMethod)) {
                    return
                }
                
                // Check containing class using cache
                val containingClass = psiMethod.containingClass ?: return
                val qualifiedName = containingClass.qualifiedName ?: return
                
                if (!isTestClass(containingClass, qualifiedName)) {
                    return
                }

                val context = CaseCheckContext.create(psiMethod)
                UnittestFileInspectorCommand(holder, project, context).execute()
            }

            private fun hasTestAnnotation(psiMethod: PsiMethod): Boolean {
                return psiMethod.annotations.any { annotation ->
                    annotation.qualifiedName in testAnnotations
                }
            }

            private fun isTestClass(psiClass: PsiClass, qualifiedName: String): Boolean {
                return testClassCache.getOrPut(qualifiedName) {
                    // First check class name pattern (fastest check)
                    val className = psiClass.name
                    if (className != null && (
                        className.endsWith("Test") || 
                        className.endsWith("Tests") || 
                        className.endsWith("TestCase")
                    )) {
                        return@getOrPut true
                    }

                    // Then check annotations if needed
                    psiClass.annotations.any { 
                        it.qualifiedName in testClassAnnotations 
                    }
                }
            }
        }
    }

    override fun getID(): String = "PitestUnitTest"

    // Clear cache when plugin is unloaded
    fun clearCache() {
        testClassCache.clear()
    }
}