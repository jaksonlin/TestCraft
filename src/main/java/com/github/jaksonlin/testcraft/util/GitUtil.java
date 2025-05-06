package com.github.jaksonlin.testcraft.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

public class GitUtil {
    private static final Logger logger = Logger.getInstance(GitUtil.class);
    private static final ThreadPoolExecutor gitThreadPool = new ThreadPoolExecutor(
        4, // core pool size
        8, // max pool size
        10L, // reduced keep alive time to release threads faster
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(50),
        new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "GitCommandThread-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        },
        new ThreadPoolExecutor.AbortPolicy()
    ) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            purge();
            allowCoreThreadTimeOut(true);
        }
    };
    private static final long GIT_COMMAND_TIMEOUT = 30; // seconds

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

    private static CompletableFuture<List<String>> runGitCommandAsync(GitLineHandler handler) {
        Git git = Git.getInstance();
        boolean inDispatchThread = ApplicationManager.getApplication().isDispatchThread();
        
        Callable<List<String>> runCommandCallable = () -> {
            try {
                logger.info("Starting git command execution in thread: " + Thread.currentThread().getName());
                List<String> result = git.runCommand(handler).getOutput();
                logger.info("Completed git command execution in thread: " + Thread.currentThread().getName());
                return result;
            } catch (Exception e) {
                logger.error("Error executing git command in thread " + Thread.currentThread().getName(), e);
                throw e;
            } finally {
                gitThreadPool.purge();
            }
        };

        CompletableFuture<List<String>> future = new CompletableFuture<>();
        
        try {
            logger.info("Submitting git command to thread pool. Current active threads: " +
                gitThreadPool.getActiveCount() + ", Queue size: " + gitThreadPool.getQueue().size());
            
            gitThreadPool.submit(() -> {
                try {
                    List<String> result = runCommandCallable.call();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            
            // Set a timeout
            long timeout = inDispatchThread ? 5 : GIT_COMMAND_TIMEOUT;
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                if (!future.isDone()) {
                    future.completeExceptionally(new TimeoutException("Git command timed out after " + timeout + " seconds"));
                }
                scheduler.shutdown();
            }, timeout, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }

    private static List<String> runGitCommand(GitLineHandler handler) {
        try {
            return runGitCommandAsync(handler).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to execute git command", e);
            throw new RuntimeException("Git command execution failed: " + e.getMessage(), e);
        }
    }

    public static GitCommitInfo getLastCommitInfo(Project project, PsiFile file) {
        VirtualFile virtualFile = file.getVirtualFile();
        GitRepository repository = virtualFile != null ? getRepositoryForFile(project, virtualFile) : null;
        if (repository == null) {
            return null;
        }

        GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.LOG);
       
        List<String> gitCommandArgs = new ArrayList<>();
        gitCommandArgs.add("--max-count=1");
        gitCommandArgs.add("--pretty=format:%an|%ae|%ad|%s");
        gitCommandArgs.add("--date=format:%Y-%m-%d %H:%M:%S");
        gitCommandArgs.add("--");
        gitCommandArgs.add(virtualFile.getPath());
        handler.addParameters(gitCommandArgs);
        StringBuilder command = new StringBuilder();

        for (String arg : gitCommandArgs) { 
            command.append(arg).append(" ");
        }
        logger.info("Running git command: " + command.toString());

            List<String> output = runGitCommand(handler);
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

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        int startOffset = psiMethod.getTextRange().getStartOffset();
        int endOffset = psiMethod.getTextRange().getEndOffset();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(endOffset) + 1;

        GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.BLAME);
        
        List<String> gitCommandArgs = new ArrayList<>();
        gitCommandArgs.add("-L");
        gitCommandArgs.add(startLine + "," + endLine);
        gitCommandArgs.add("--porcelain");
        gitCommandArgs.add(file.getPath());
        handler.addParameters(gitCommandArgs);
        StringBuilder command = new StringBuilder();
        for (String arg : gitCommandArgs) {
            command.append(arg).append(" ");
        }
        logger.info("Running git command: " + command.toString());


            List<String> output = runGitCommand(handler);

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

        return null;
    }

    public static GitUserInfo getLastModifyInfo(Project project, PsiMethod psiMethod) {
        try {
            PsiFile containingFile = psiMethod.getContainingFile();
            VirtualFile file = containingFile != null ? containingFile.getVirtualFile() : null;
            if (file == null) {
                logger.error("Cannot get virtual file for method: " + psiMethod.getName());
                return getGitUserInfo(project);
            }
            
            GitRepository repository = getRepositoryForFile(project, file);
            if (repository == null) {
                logger.error("Cannot find git repository for file: " + file.getPath());
                return getGitUserInfo(project);
            }

            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                logger.error("Cannot get document for file: " + file.getPath());
                return getGitUserInfo(project);
            }
            
            int startOffset = psiMethod.getTextRange().getStartOffset();
            int endOffset = psiMethod.getTextRange().getEndOffset();
            int startLine = document.getLineNumber(startOffset) + 1;
            int endLine = document.getLineNumber(endOffset) + 1;

            GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.BLAME);

            List<String> gitCommandArgs = new ArrayList<>();
            gitCommandArgs.add("-L");
            gitCommandArgs.add(startLine + "," + endLine);
            gitCommandArgs.add("--porcelain");
            gitCommandArgs.add(file.getPath());
            handler.addParameters(gitCommandArgs);
            StringBuilder command = new StringBuilder();
            for (String arg : gitCommandArgs) {
                command.append(arg).append(" ");
            }
            logger.info("Running git command: " + command.toString());

            List<String> output = runGitCommand(handler);
            if (output == null || output.isEmpty()) {
                logger.info("No output from git blame command");
                return getGitUserInfo(project);
            }

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
                        logger.error("Error parsing timestamp from git blame output", e);
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

            // If we couldn't get the last modify info, return current user info
            logger.info("Could not determine last modifier, using current user info");
            return getGitUserInfo(project);
            
        } catch (Exception e) {
            logger.error("Error getting last modify info", e);
            return getGitUserInfo(project);
        }
    }

    public static CompletableFuture<GitUserInfo> getGitUserInfoAsync(Project project) {
        GitRepositoryManager repositoryManager = getRepositoryManager(project);
        List<GitRepository> repositories = repositoryManager.getRepositories();
        GitRepository repository = repositories.isEmpty() ? null : repositories.get(0);

        if (repository != null) {
            CompletableFuture<String> nameFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return GitConfigUtil.getValue(project, repository.getRoot(), GitConfigUtil.USER_NAME);
                } catch (Exception e) {
                    logger.error("Error getting git user name", e);
                    return "Unknown";
                }
            }, gitThreadPool);

            CompletableFuture<String> emailFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return GitConfigUtil.getValue(project, repository.getRoot(), GitConfigUtil.USER_EMAIL);
                } catch (Exception e) {
                    logger.error("Error getting git user email", e);
                    return "unknown@email.com";
                }
            }, gitThreadPool);

            return CompletableFuture.allOf(nameFuture, emailFuture)
                .thenApply(v -> new GitUserInfo(
                    nameFuture.join() != null ? nameFuture.join() : "Unknown",
                    emailFuture.join() != null ? emailFuture.join() : "unknown@email.com"
                ));
        }
        return CompletableFuture.completedFuture(new GitUserInfo("Unknown", "unknown@email.com"));
    }

    public static GitUserInfo getGitUserInfo(Project project) {
        try {
            return getGitUserInfoAsync(project).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to get git user info", e);
            return new GitUserInfo("Unknown", "unknown@email.com");
        }
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

