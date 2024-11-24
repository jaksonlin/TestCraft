package com.github.jaksonlin.pitestintellij.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.config.GitConfigUtil
import git4idea.repo.GitConfig
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiMethod

object GitUtil {
    fun getRepositoryManager(project: Project): GitRepositoryManager {
        return GitRepositoryManager.getInstance(project)
    }

    fun getCurrentRepository(project: Project, element: PsiElement): GitRepository? {
        val file = element.containingFile?.virtualFile
        return file?.let { getRepositoryForFile(project, it) }
    }

    fun getRepositoryForFile(project: Project, file: VirtualFile): GitRepository? {
        return GitRepositoryManager.getInstance(project).getRepositoryForFile(file)
    }

    fun getGitUserInfo(project: Project): GitUserInfo {
        val repository = getRepositoryManager(project).repositories.firstOrNull()
        return repository?.let {
            val name = ApplicationManager.getApplication().executeOnPooledThread<String?> {
                GitConfigUtil.getValue(project, repository.root, GitConfigUtil.USER_NAME)
            }.get()
            val email = ApplicationManager.getApplication().executeOnPooledThread<String?> {
                GitConfigUtil.getValue(project, repository.root, GitConfigUtil.USER_EMAIL)
            }.get()
            GitUserInfo(
                name = name ?: "Unknown",
                email = email ?: "unknown@email.com"
            )
        } ?: GitUserInfo("Unknown", "unknown@email.com")
    }

    

    fun getLastCommitInfo(project: Project, file: PsiFile): GitCommitInfo? {
        val repository = file.virtualFile?.let { getRepositoryForFile(project, it) } ?: return null
        val git = Git.getInstance()
        
        val handler = GitLineHandler(
            project,
            repository.root,
            GitCommand.LOG
        ).apply {
            addParameters(
                "--max-count=1",
                "--pretty=format:%an|%ae|%ad|%s",
                "--date=format:%Y-%m-%d %H:%M:%S",
                "--",
                file.virtualFile.path
            )
        }
        val output = ApplicationManager.getApplication().executeOnPooledThread<List<String>> {
            git.runCommand(handler).output
        }.get()
        return output.firstOrNull()?.let { line ->
            val parts = line.split("|")
            if (parts.size >= 4) {
                GitCommitInfo(
                    author = parts[0],
                    email = parts[1],
                    date = parts[2],
                    message = parts[3]
                )
            } else null
        }
    }

    fun getLastModifyInfo(project: Project, psiMethod: PsiMethod): GitUserInfo? {
        val file = psiMethod.containingFile?.virtualFile ?: return null
        val repository = getRepositoryForFile(project, file) ?: return null
        val git = Git.getInstance()

        // Get method's line range
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return null
        val startOffset = psiMethod.textRange.startOffset
        val endOffset = psiMethod.textRange.endOffset
        val startLine = document.getLineNumber(startOffset) + 1 // Git blame uses 1-based line numbers
        val endLine = document.getLineNumber(endOffset) + 1

        val handler = GitLineHandler(
            project,
            repository.root,
            GitCommand.BLAME
        ).apply {
            addParameters(
                "-L", "$startLine,$endLine", // Limit blame to method's lines
                "--porcelain", // Get detailed output
                file.path
            )
        }

        val output = ApplicationManager.getApplication().executeOnPooledThread<List<String>> {
            git.runCommand(handler).output
        }.get()
        
        // Parse blame output to get the most recent commit
        var latestCommit: BlameInfo? = null
        var currentCommit: BlameInfo? = null
        
        output.forEach { line ->
            when {
                line.startsWith("author ") -> {
                    val authorName = line.substringAfter("author ").trim()
                    if (authorName == "Not Committed Yet") {
                        val gitUserInfo = getGitUserInfo(project)
                        currentCommit = currentCommit?.copy(
                            author = gitUserInfo.name,
                            email = gitUserInfo.email
                        ) ?: BlameInfo(author = gitUserInfo.name, email = gitUserInfo.email)
                    } else {
                        currentCommit = currentCommit?.copy(
                            author = authorName
                        ) ?: BlameInfo(author = authorName)
                    }
                }
                line.startsWith("author-mail ") -> {
                    val email = line.substringAfter("author-mail ").trim().removeSurrounding("<", ">")
                    if (email == "not.committed.yet") {
                        val gitUserInfo = getGitUserInfo(project)
                        currentCommit = currentCommit?.copy(
                            author = gitUserInfo.name,
                            email = gitUserInfo.email
                        ) ?: BlameInfo(author = gitUserInfo.name, email = gitUserInfo.email)
                    } else {
                        currentCommit = currentCommit?.copy(
                            email = email
                        ) ?: BlameInfo(author = currentCommit?.author ?: "", email = email)
                    }
                }
                line.startsWith("author-time ") -> {
                    val timestamp = line.substringAfter("author-time ").trim().toLongOrNull()
                    if (timestamp != null) {
                        currentCommit = currentCommit?.copy(timestamp = timestamp)
                        if (latestCommit == null || (currentCommit?.timestamp ?: 0) > (latestCommit?.timestamp ?: 0)) {
                            currentCommit?.let { latestCommit = it }
                        }
                    }
                    currentCommit = null // Reset for next commit
                }
            }
        }

        return latestCommit?.let {
            GitUserInfo(
                name = it.author,
                email = it.email ?: "unknown@email.com",
                timestamp = it.timestamp ?: System.currentTimeMillis() // If timestamp is not available, use current time
            )
        }
    }

    private data class BlameInfo(
        val author: String,
        val email: String? = null,
        val timestamp: Long = 0
    )
}

data class GitUserInfo(
    val name: String,
    val email: String,
    val timestamp: Long = 0
) {
    override fun toString(): String = "$name <$email>"
}

data class GitCommitInfo(
    val author: String,
    val email: String,
    val date: String,
    val message: String
)
