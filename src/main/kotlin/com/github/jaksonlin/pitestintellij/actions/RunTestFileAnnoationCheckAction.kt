package com.github.jaksonlin.pitestintellij.actions

import com.github.jaksonlin.pitestintellij.commands.unittestannotations.CheckAnnotationCommand
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod

class RunTestFileAnnoationCheckAction: AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        batchCheckAnnotation(e)
    }

    private fun batchCheckAnnotation(e: AnActionEvent){
        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)
            val psiJavaFile = psiFile as PsiJavaFile

            psiJavaFile.accept(object : JavaRecursiveElementVisitor() {
                override fun visitMethod(method: PsiMethod) {
                    super.visitMethod(method)
                    // inspect the method annotations
                    val annotations = method.annotations
                    for (annotation in annotations) {
                        // inspect the annotation
                        val annotationName = annotation.qualifiedName
                        if (annotationName!=null && // to support junit 4 & 5, do not use regexp, as it will also match some beforeTest/afterTest annotations
                            (annotationName == "org.junit.Test" || annotationName == "org.junit.jupiter.api.Test" || annotationName == "Test")) {
                            val context = CaseCheckContext.create(method)
                            CheckAnnotationCommand(e.project!!, context).execute()
                            break
                        }
                    }
                }
            })
    }
}