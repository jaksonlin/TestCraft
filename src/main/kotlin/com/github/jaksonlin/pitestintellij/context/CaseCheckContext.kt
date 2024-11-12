package com.github.jaksonlin.pitestintellij.context

import AnnotationConfigService
import com.github.jaksonlin.pitestintellij.actions.RunCaseAnnoationCheckAction
import com.github.jaksonlin.pitestintellij.annotations.AnnotationParser
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiMethod

data class CaseCheckContext(
    val psiMethod: PsiMethod,
    val schema: AnnotationSchema,
    val parser: AnnotationParser
) {
    companion object {
        fun create(psiMethod: PsiMethod): CaseCheckContext? {
            val configService = service<AnnotationConfigService>()
            val schema = configService.getSchema()
            return CaseCheckContext(
                psiMethod = psiMethod,
                schema = schema,
                parser = AnnotationParser(schema)
            )
        }
    }
}