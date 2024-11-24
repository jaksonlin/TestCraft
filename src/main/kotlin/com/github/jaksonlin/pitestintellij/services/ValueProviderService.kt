package com.github.jaksonlin.pitestintellij.services

import com.github.jaksonlin.pitestintellij.annotations.AnnotationFieldType
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.annotations.ValueProvider
import com.github.jaksonlin.pitestintellij.annotations.ValueProviderType
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.utils.GitUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
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
            ValueProviderType.CLASS_NAME -> getClassName(context.psiClass)
            ValueProviderType.METHOD_NAME -> getMethodName(context.psiMethod)
            ValueProviderType.METHOD_SIGNATURE -> getMethodSignature(context.psiMethod)
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

    private fun getClassName(psiClass: PsiClass): String {
        return psiClass.qualifiedName ?: psiClass.name ?: ""
    }

    private fun getMethodName(psiMethod: PsiMethod): String {
        return psiMethod.name
    }

    private fun getMethodSignature(psiMethod: PsiMethod): String {
        return buildString {
            append(psiMethod.name)
            append("(")
            append(psiMethod.parameterList.parameters.joinToString(", ") { param ->
                "${param.name}: ${param.type.presentableText}"
            })
            append(")")
            psiMethod.returnType?.let { append(": ${it.presentableText}") }
        }
    }
}

private fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else this
}