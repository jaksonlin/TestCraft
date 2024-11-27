package com.github.jaksonlin.pitestintellij.services

import com.github.jaksonlin.pitestintellij.annotations.ValueProvider
import com.github.jaksonlin.pitestintellij.annotations.ValueProviderType
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.util.GitUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import java.text.SimpleDateFormat
import java.util.*

@Service(Service.Level.PROJECT)
class ValueProviderService(private val project: Project) {
    fun provideValue(provider: ValueProvider, context: CaseCheckContext): Any? {
        return when (provider.type) {
            ValueProviderType.GIT_AUTHOR -> getGitAuthor()
            ValueProviderType.LAST_MODIFIER_AUTHOR -> getLastModifierAuthor(context.psiMethod)
            ValueProviderType.LAST_MODIFIER_TIME -> getLastModifierTime(context.psiMethod)
            ValueProviderType.CURRENT_DATE -> getCurrentDate(provider.format)
            ValueProviderType.METHOD_NAME_BASED -> generateDescription(context.psiMethod)
            ValueProviderType.FIXED_VALUE -> provider.value
            ValueProviderType.CLASS_NAME -> guessClassUnderTestClassName(context.psiClass) // return the qualified name for the  class under test
            ValueProviderType.METHOD_NAME -> guessMethodUnderTestMethodName(context.psiMethod) // return the method name for the method under test
            ValueProviderType.METHOD_SIGNATURE -> tryGetMethodUnderTestSignature(context.psiClass, context.psiMethod) 
                ?: ""
            ValueProviderType.FIRST_CREATOR_AUTHOR -> getFirstCreatorAuthor(context.psiMethod)
            ValueProviderType.FIRST_CREATOR_TIME -> getFirstCreatorTime(context.psiMethod)
        }
    }

    private fun getGitAuthor(): String {
        return GitUtil.getGitUserInfo(project).toString()
    }
    
    private fun getLastModifierAuthor(psiMethod: PsiMethod): String {
        return GitUtil.getLastModifyInfo(project, psiMethod)?.toString()
            ?: getGitAuthor()
    }

    private fun getLastModifierTime(psiMethod: PsiMethod): String {
        var timestamp = GitUtil.getLastModifyInfo(project, psiMethod)?.timestamp
        timestamp = timestamp?.times(1000)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(Date((timestamp ?: System.currentTimeMillis())))
    }

    private fun getCurrentDate(format: String?): String {
        val dateFormat = SimpleDateFormat(format ?: "yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(Date())
    }

    private fun generateDescription(psiMethod: PsiMethod): String {
        val methodName = psiMethod.name
        // Convert camelCase or snake_case to space-separated words
        return methodName.replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replace("_", " ")
            .lowercase()
            .capitalizeFirst()
    }

    private fun guessClassUnderTestClassName(psiClass: PsiClass): String {
        // Get base class name without Test prefix/suffix
        val baseClassName = psiClass.name?.removePrefix("Test")?.removeSuffix("Test") ?: return ""
        
        // Get test class package and normalize it
        val testPackage = psiClass.qualifiedName
            ?.removeSuffix(psiClass.name!!)
            ?.removeSuffix(".")
            ?.replace(".test.", ".")
            ?.replace(".tests.", ".")
            ?.removeSuffix(".test")
            ?.removeSuffix(".tests")
            ?: return ""
            
        return "$testPackage.$baseClassName"
    }

    private fun guessMethodUnderTestMethodName(psiMethod: PsiMethod): String {
        return psiMethod.name
            .removePrefix("test")
            .removePrefix("should")
            .removePrefix("testShould")
            // Take only the part before first underscore if it exists
            .split("_")
            .first()
            .lowercase() // just lowercase for case-insensitive matching
    }

    private fun tryGetMethodUnderTestSignature(psiClass: PsiClass, psiMethod: PsiMethod): String? {
        val guessedClassName = guessClassUnderTestClassName(psiClass)
        val guessedMethodName = guessMethodUnderTestMethodName(psiMethod)
            .lowercase() // normalize to lowercase for comparison
        
        // Find the class under test using PSI API
        val project = psiClass.project
        val psiManager = PsiManager.getInstance(project)
        val psiFacade = JavaPsiFacade.getInstance(project)
        
        // Try to find the class under test
        val classUnderTest = psiFacade.findClass(guessedClassName, GlobalSearchScope.projectScope(project))
            ?: return null
        
        // Skip if the target class is a test class
        val hasTestMethods = classUnderTest.methods.any { method ->
            method.annotations.any { annotation ->
                val annotationName = annotation.qualifiedName
                annotationName == "org.junit.jupiter.api.Test" || // JUnit 5
                annotationName == "org.junit.Test"             ||  // JUnit 4
                annotationName?.contains("Test") ?: false // Custom test annotation
            }
        }
        
        if (hasTestMethods) {
            return null
        }
            
        // Find matching method in the class under test
        val methodUnderTest = classUnderTest.methods.firstOrNull { method ->
            method.name.lowercase().contains(guessedMethodName.lowercase())
        } ?: return null
        
        // Return the method signature
        return getMethodSignature(methodUnderTest)
    }

    private fun getMethodSignature(psiMethod: PsiMethod): String {
        return buildString {
            // Add visibility modifier (excluding annotations)
            psiMethod.modifierList.text.trim()
                .split(" ")
                .filter { it.isNotEmpty() && !it.startsWith("@") }
                .joinTo(this, " ")
            append(" ")
            
            // Add return type if not constructor
            if (!psiMethod.isConstructor) {
                append(psiMethod.returnType?.presentableText ?: "void")
                append(" ")
            }
            
            // Add method name and parameters
            append(psiMethod.name)
            append("(")
            append(psiMethod.parameterList.parameters.joinToString(", ") { param ->
                "${param.type.presentableText} ${param.name}"
            })
            append(")")
            
            // Add throws clause if present
            val throwsList = psiMethod.throwsList.referencedTypes
            if (throwsList.isNotEmpty()) {
                append(" throws ")
                append(throwsList.joinToString(", ") { it.presentableText })
            }
        }.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
    }

    private fun getFirstCreatorAuthor(psiMethod: PsiMethod): String {
        return GitUtil.getFirstCreatorInfo(project, psiMethod)?.toString()
            ?: getGitAuthor()
    }

    private fun getFirstCreatorTime(psiMethod: PsiMethod): String {
        var timestamp = GitUtil.getFirstCreatorInfo(project, psiMethod)?.timestamp
        timestamp = timestamp?.times(1000)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(Date((timestamp ?: System.currentTimeMillis())))
    }
}

private fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else this
}