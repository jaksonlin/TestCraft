package com.github.jaksonlin.pitestintellij.commands.pitest

import com.github.jaksonlin.pitestintellij.context.PitestContext
import com.intellij.openapi.project.Project

class StoreHistoryCommand  (project: Project, context: PitestContext) : PitestCommand(project, context) {

    override fun execute() {
        runHistoryManager.saveRunHistory(context)
    }
}