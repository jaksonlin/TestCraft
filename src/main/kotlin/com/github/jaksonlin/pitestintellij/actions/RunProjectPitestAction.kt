package com.github.jaksonlin.pitestintellij.actions

import com.github.jaksonlin.pitestintellij.services.PitestService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

class RunProjectPitestAction: AnAction() {
    private val pitestService = service<PitestService>()
    override fun actionPerformed(e: AnActionEvent) {
        val targetProject = e.project ?: return

        // Retrieve all test files in the project
        val testFiles = mutableListOf<VirtualFile>()
        val fileTypeManager = FileTypeManager.getInstance()
        val virtualFileManager = VirtualFileManager.getInstance()

        virtualFileManager.refreshAndFindFileByUrl(targetProject.basePath!!)?.let { root ->
            root.refresh(false, true)
            root.children.filter { file ->
                fileTypeManager.getFileTypeByFileName(file.name).defaultExtension == "java"
            }.forEach { file ->
                if (file.name.startsWith("Test", ignoreCase = true) || file.name.endsWith("Test", ignoreCase = true)) {
                    testFiles.add(file)
                }
            }
        }
        // Run PIT on each test file
        val testFilePaths = testFiles.map { it.path }
        pitestService.runPitestBatch(targetProject, testFilePaths)

    }
}