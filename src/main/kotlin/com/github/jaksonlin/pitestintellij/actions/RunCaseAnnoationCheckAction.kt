package com.github.jaksonlin.pitestintellij.actions

import com.github.jaksonlin.pitestintellij.commands.casecheck.CheckAnnotationCommand
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class RunCaseAnnoationCheckAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {

        val psiMethod = findMethodAtCaret(e) ?: return
        val context = CaseCheckContext.create(psiMethod) ?: return
        CheckAnnotationCommand(e.project!!, context).execute()
    }



    private fun findMethodAtCaret(e: AnActionEvent): PsiMethod? {
        val project = e.project ?: return null
        val editor = e.dataContext.getData(CommonDataKeys.EDITOR) ?: return null
        val caret = e.dataContext.getData(CommonDataKeys.CARET) ?: return null
        val elementAtCaret = PsiDocumentManager.getInstance(project)
            .getPsiFile(editor.document)?.findElementAt(caret.offset) ?: return null
        return PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod::class.java)
    }

}