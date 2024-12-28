package com.github.jaksonlin.pitestintellij.services

import com.github.jaksonlin.pitestintellij.commands.CommandCancellationException
import com.github.jaksonlin.pitestintellij.commands.pitest.*
import com.github.jaksonlin.pitestintellij.context.PitestContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import com.jetbrains.rd.util.ExecutionException
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JTextArea

@Service(Service.Level.APP)
class PitestService {

    fun runPitest(targetProject: Project, testFilePath: String) {
        val context = PitestContext(testFilePath, System.currentTimeMillis())

        val commands = listOf(
            PrepareEnvironmentCommand(targetProject, context),
            BuildPitestCommandCommand(targetProject, context),
            RunPitestCommand(targetProject, context),
            HandlePitestResultCommand(targetProject, context),
            StoreHistoryCommand(targetProject, context),
        )

        object : Task.Backgroundable(targetProject, "Running pitest", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    for (command in commands) {
                        if (indicator.isCanceled) {
                            Messages.showInfoMessage("Pitest run was canceled", "Canceled")
                            break
                        }
                        command.execute()
                    }
                } catch(e: CommandCancellationException) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage("Pitest run was canceled", "Canceled")
                    }
                } catch (e: ExecutionException) {
                    if (e.cause is CommandCancellationException) {
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showInfoMessage("Pitest run was canceled", "Canceled")
                        }
                    } else {
                        showErrorDialog(e, context)
                    }
                } catch (e: Exception) {
                    showErrorDialog(e, context)
                }
            }
        }.queue()
    }


    private fun showErrorDialog(e: Exception, context: PitestContext) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()
        val contextInformation = PitestContext.dumpPitestContext(context)
        val errorMessage = "Error executing Pitest command: ${e.message}; $contextInformation\n$stackTrace"

        val textArea = JTextArea(errorMessage)
        textArea.isEditable = false
        textArea.lineWrap = true
        textArea.wrapStyleWord = true

        val scrollPane = JBScrollPane(textArea)
        scrollPane.preferredSize = java.awt.Dimension(640, 480)

        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(scrollPane, errorMessage)
        }
    }
}