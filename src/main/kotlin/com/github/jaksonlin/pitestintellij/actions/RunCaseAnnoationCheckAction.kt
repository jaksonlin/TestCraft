package com.github.jaksonlin.pitestintellij.actions

import com.github.jaksonlin.pitestintellij.commands.unittestannotations.CheckAnnotationCommand
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil

class RunCaseAnnoationCheckAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val psiMethodInfo = findMethodAtCaret(e) ?: return
        val context = CaseCheckContext.create(psiMethodInfo.first, psiMethodInfo.second)
        CheckAnnotationCommand(e.project!!, context).execute()
    }

    private fun findMethodAtCaret(e: AnActionEvent): Pair<PsiMethod, PsiClass>? {
        val project = e.project ?: return null
        val editor = e.dataContext.getData(CommonDataKeys.EDITOR) ?: return null
        val caret = e.dataContext.getData(CommonDataKeys.CARET) ?: return null
        val elementAtCaret = PsiDocumentManager.getInstance(project)
            .getPsiFile(editor.document)?.findElementAt(caret.offset) ?: return null
        val method = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod::class.java) ?: return null
        val containingClass = PsiTreeUtil.getParentOfType(method, PsiClass::class.java) ?: return null
        return method to containingClass
    }

}