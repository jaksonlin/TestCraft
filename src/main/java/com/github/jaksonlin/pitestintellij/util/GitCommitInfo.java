package com.github.jaksonlin.pitestintellij.util;

public class GitCommitInfo {
    private final String author;
    private final String email;
    private final String date;
    private final String message;

    public GitCommitInfo(String author, String email, String date, String message) {
        this.author = author;
        this.email = email;
        this.date = date;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }
}
