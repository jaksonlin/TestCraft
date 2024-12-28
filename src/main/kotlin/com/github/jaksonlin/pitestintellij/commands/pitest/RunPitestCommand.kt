package com.github.jaksonlin.pitestintellij.commands.pitest

import com.github.jaksonlin.pitestintellij.context.PitestContext
import com.github.jaksonlin.pitestintellij.util.ProcessExecutor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.file.Paths

class RunPitestCommand (project: Project, context: PitestContext) : PitestCommand(project, context) {
    override fun execute() {
        val command = context.command
        if (command.isEmpty()){
            throw IllegalStateException("Pitest command not set")
        }

        thisLogger().info("Run pitest with command: ${command.joinToString(" ")}")
        File(Paths.get(context.classpathFileDirectory!!, "command.txt").toString()).writeText(command.joinToString(" "))

        context.processResult = ProcessExecutor.executeProcess(command)
    }

}