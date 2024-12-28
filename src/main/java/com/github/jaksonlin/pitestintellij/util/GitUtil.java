package com.github.jaksonlin.pitestintellij.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import git4idea.config.GitConfigUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class GitUtil {

    public static GitRepositoryManager getRepositoryManager(Project project) {
        return GitRepositoryManager.getInstance(project);
    }

    public static GitRepository getCurrentRepository(Project project, PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        VirtualFile file = containingFile != null ? containingFile.getVirtualFile() : null;
        return file != null ? getRepositoryForFile(project, file) : null;
    }

    public static GitRepository getRepositoryForFile(Project project, VirtualFile file) {
        return GitRepositoryManager.getInstance(project).getRepositoryForFile(file);
    }

    public static GitUserInfo getGitUserInfo(Project project) {
        GitRepositoryManager repositoryManager = getRepositoryManager(project);
        List<GitRepository> repositories = repositoryManager.getRepositories();
        GitRepository repository = repositories.isEmpty() ? null : repositories.get(0);

        if (repository != null) {
            try {
                Callable<String> getNameCallable = () -> GitConfigUtil.getValue(project, repository.getRoot(), GitConfigUtil.USER_NAME);
                String name = ApplicationManager.getApplication().executeOnPooledThread(getNameCallable).get();

                Callable<String> getEmailCallable = () -> GitConfigUtil.getValue(project, repository.getRoot(), GitConfigUtil.USER_EMAIL);
                String email = ApplicationManager.getApplication().executeOnPooledThread(getEmailCallable).get();

                return new GitUserInfo(
                        name != null ? name : "Unknown",
                        email != null ? email : "unknown@email.com"
                );
            } catch (InterruptedException | ExecutionException e) {
                // Handle exception appropriately, maybe log it
                e.printStackTrace();
                return new GitUserInfo("Unknown", "unknown@email.com");
            }
        }
        return new GitUserInfo("Unknown", "unknown@email.com");
    }

    public static GitCommitInfo getLastCommitInfo(Project project, PsiFile file) {
        VirtualFile virtualFile = file.getVirtualFile();
        GitRepository repository = virtualFile != null ? getRepositoryForFile(project, virtualFile) : null;
        if (repository == null) {
            return null;
        }
        Git git = Git.getInstance();

        GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.LOG);
        handler.addParameters(
                "--max-count=1",
                "--pretty=format:%an|%ae|%ad|%s",
                "--date=format:%Y-%m-%d %H:%M:%S",
                "--",
                virtualFile.getPath()
        );

        try {
            Callable<List<String>> runCommandCallable = () -> git.runCommand(handler).getOutput();
            List<String> output = ApplicationManager.getApplication().executeOnPooledThread(runCommandCallable).get();
            String firstLine = output.isEmpty() ? null : output.get(0);
            if (firstLine != null) {
                String[] parts = firstLine.split("\\|");
                if (parts.length >= 4) {
                    return new GitCommitInfo(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3]
                    );
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            // Handle exception appropriately, maybe log it
            e.printStackTrace();
        }
        return null;
    }

    public static GitUserInfo getFirstCreatorInfo(Project project, PsiMethod psiMethod) {
        PsiFile containingFile = psiMethod.getContainingFile();
        VirtualFile file = containingFile != null ? containingFile.getVirtualFile() : null;
        if (file == null) {
            return null;
        }
        GitRepository repository = getRepositoryForFile(project, file);
        if (repository == null) {
            return null;
        }
        Git git = Git.getInstance();

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        int startOffset = psiMethod.getTextRange().getStartOffset();
        int endOffset = psiMethod.getTextRange().getEndOffset();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(endOffset) + 1;

        GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.BLAME);
        handler.addParameters(
                "-L", startLine + "," + endLine,
                "--porcelain",
                file.getPath()
        );

        try {
            Callable<List<String>> runCommandCallable = () -> git.runCommand(handler).getOutput();
            List<String> output = ApplicationManager.getApplication().executeOnPooledThread(runCommandCallable).get();

            String authorName = null;
            String email = null;
            Long timestamp = null;

            for (String line : output) {
                if (line.startsWith("author ")) {
                    authorName = line.substring("author ".length()).trim();
                } else if (line.startsWith("author-mail ")) {
                    email = line.substring("author-mail ".length()).trim().replaceAll("^<|>$", "");
                } else if (line.startsWith("author-time ")) {
                    try {
                        timestamp = Long.parseLong(line.substring("author-time ".length()).trim());
                        if (authorName != null && email != null) {
                            break;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            if ("Not Committed Yet".equals(authorName) || "not.committed.yet".equals(email)) {
                GitUserInfo gitUserInfo = getGitUserInfo(project);
                return new GitUserInfo(
                        gitUserInfo.getName(),
                        gitUserInfo.getEmail(),
                        timestamp != null ? timestamp : System.currentTimeMillis()
                );
            }

            if (authorName != null && email != null) {
                return new GitUserInfo(
                        authorName,
                        email,
                        timestamp != null ? timestamp : System.currentTimeMillis()
                );
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GitUserInfo getLastModifyInfo(Project project, PsiMethod psiMethod) {
        PsiFile containingFile = psiMethod.getContainingFile();
        VirtualFile file = containingFile != null ? containingFile.getVirtualFile() : null;
        if (file == null) {
            return null;
        }
        GitRepository repository = getRepositoryForFile(project, file);
        if (repository == null) {
            return null;
        }
        Git git = Git.getInstance();

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        int startOffset = psiMethod.getTextRange().getStartOffset();
        int endOffset = psiMethod.getTextRange().getEndOffset();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(endOffset) + 1;

        GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.BLAME);
        handler.addParameters(
                "-L", startLine + "," + endLine,
                "--porcelain",
                file.getPath()
        );

        try {
            Callable<List<String>> runCommandCallable = () -> git.runCommand(handler).getOutput();
            List<String> output = ApplicationManager.getApplication().executeOnPooledThread(runCommandCallable).get();

            BlameInfo latestCommit = null;
            BlameInfo currentCommit = null;

            for (String line : output) {
                if (line.startsWith("author ")) {
                    String authorName = line.substring("author ".length()).trim();
                    if ("Not Committed Yet".equals(authorName)) {
                        GitUserInfo gitUserInfo = getGitUserInfo(project);
                        currentCommit = (currentCommit == null ? new BlameInfo() : currentCommit).toBuilder()
                                .author(gitUserInfo.getName())
                                .email(gitUserInfo.getEmail())
                                .build();
                    } else {
                        currentCommit = (currentCommit == null ? new BlameInfo() : currentCommit).toBuilder()
                                .author(authorName)
                                .build();
                    }
                } else if (line.startsWith("author-mail ")) {
                    String email = line.substring("author-mail ".length()).trim().replaceAll("^<|>$", "");
                    if ("not.committed.yet".equals(email)) {
                        GitUserInfo gitUserInfo = getGitUserInfo(project);
                        currentCommit = (currentCommit == null ? new BlameInfo() : currentCommit).toBuilder()
                                .author(gitUserInfo.getName())
                                .email(gitUserInfo.getEmail())
                                .build();
                    } else {
                        currentCommit = (currentCommit == null ? new BlameInfo() : currentCommit).toBuilder()
                                .email(email)
                                .build();
                    }
                } else if (line.startsWith("author-time ")) {
                    try {
                        long timestamp = Long.parseLong(line.substring("author-time ".length()).trim());
                        currentCommit = (currentCommit == null ? new BlameInfo() : currentCommit).toBuilder()
                                .timestamp(timestamp)
                                .build();
                        if (latestCommit == null || currentCommit.getTimestamp() > latestCommit.getTimestamp()) {
                            latestCommit = currentCommit;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    currentCommit = null;
                }
            }

            if (latestCommit != null) {
                return new GitUserInfo(
                        latestCommit.getAuthor(),
                        latestCommit.getEmail() != null ? latestCommit.getEmail() : "unknown@email.com",
                        latestCommit.getTimestamp() != null ? latestCommit.getTimestamp() : System.currentTimeMillis()
                );
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class BlameInfo {
        private String author;
        private String email;
        private Long timestamp;

        public BlameInfo() {
        }

        public String getAuthor() {
            return author;
        }

        public String getEmail() {
            return email;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public BlameInfoBuilder toBuilder() {
            return new BlameInfoBuilder(this);
        }

        public static class BlameInfoBuilder {
            private String author;
            private String email;
            private Long timestamp;

            public BlameInfoBuilder() {
            }

            public BlameInfoBuilder(BlameInfo original) {
                this.author = original.author;
                this.email = original.email;
                this.timestamp = original.timestamp;
            }

            public BlameInfoBuilder author(String author) {
                this.author = author;
                return this;
            }

            public BlameInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public BlameInfoBuilder timestamp(Long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public BlameInfo build() {
                BlameInfo blameInfo = new BlameInfo();
                blameInfo.author = this.author;
                blameInfo.email = this.email;
                blameInfo.timestamp = this.timestamp;
                return blameInfo;
            }
        }
    }
}

