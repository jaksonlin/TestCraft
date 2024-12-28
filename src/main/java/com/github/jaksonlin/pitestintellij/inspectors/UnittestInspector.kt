package com.github.jaksonlin.pitestintellij.inspectors

import com.github.jaksonlin.pitestintellij.MyBundle
import com.github.jaksonlin.pitestintellij.commands.unittestannotations.UnittestFileInspectorCommand
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import java.util.concurrent.ConcurrentHashMap

class UnittestInspector : AbstractBaseJavaLocalInspectionTool() {
    override fun getGroupDisplayName(): String = MyBundle.message("inspection.group.name")
    override fun getDisplayName(): String = MyBundle.message("inspection.display.name")
    override fun getShortName(): String = "UnittestCaseAnnotationInspection"
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
                // get the psiclass for the containing class
                val psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass::class.java) ?: return
                val context = CaseCheckContext.create(psiMethod, psiClass)
                UnittestFileInspectorCommand(holder, project, context).execute()
            }

            private fun hasTestAnnotation(psiMethod: PsiMethod): Boolean {
                return psiMethod.annotations.any { annotation ->
                    annotation.qualifiedName in testAnnotations
                }
            }


        }
    }

    override fun getID(): String = "UnittestCaseAnnotationInspection"

    // Clear cache when plugin is unloaded
    fun clearCache() {
        testClassCache.clear()
    }
}