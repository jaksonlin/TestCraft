package com.github.jaksonlin.pitestintellij.util;

public class GitUserInfo {
    private final String name;
    private final String email;
    private final long timestamp;

    public GitUserInfo(String name, String email) {
        this(name, email, 0);
    }

    public GitUserInfo(String name, String email, long timestamp) {
        this.name = name;
        this.email = email;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
}
