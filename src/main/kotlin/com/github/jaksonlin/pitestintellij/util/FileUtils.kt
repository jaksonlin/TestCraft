package com.github.jaksonlin.pitestintellij.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

object FileUtils {
    fun findTargetClassFile(sourceRoots: List<String>, targetClass: String): TargetClassInfo? {
        for (sourceRoot in sourceRoots) {
            val targetClassFile = findFileRecursively(sourceRoot, targetClass)
            if (targetClassFile != null) {
                return targetClassFile
            }
        }
        return null
    }

    private fun findFileRecursively(directoryPath: String, targetClass: String): TargetClassInfo? {
        val directory = Paths.get(directoryPath)
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return null
        }

        val targetFileName = if (targetClass.contains('.')) {
            targetClass.replace('.', File.separatorChar) + ".java"
        } else {
            "$targetClass.java"
        }
        
        for (file in Files.walk(directory)
            .filter { path -> Files.isRegularFile(path) }
            .collect(Collectors.toList())) {
            
            if (file.toString().endsWith(targetFileName)) {
                val directoryParentPath = file.parent
                val indexToSrcMainJava = directoryParentPath.toString().indexOf("src${File.separator}main${File.separator}java")
                val sourceRootPath = directoryParentPath.toString().substring(0, indexToSrcMainJava)
                return TargetClassInfo(file, Paths.get(sourceRootPath))
            }
        }
        return null
    }
}

data class TargetClassInfo(val file: Path, val sourceRoot: Path)