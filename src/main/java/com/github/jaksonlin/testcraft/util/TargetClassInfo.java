package com.github.jaksonlin.testcraft.util;

import java.nio.file.Path;

public class TargetClassInfo {
    private final Path file;
    private final Path sourceRoot;

    public TargetClassInfo(Path file, Path sourceRoot) {
        this.file = file;
        this.sourceRoot = sourceRoot;
    }

    public Path getFile() {
        return file;
    }

    public Path getSourceRoot() {
        return sourceRoot;
    }
}
