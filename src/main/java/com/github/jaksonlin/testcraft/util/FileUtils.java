package com.github.jaksonlin.testcraft.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static TargetClassInfo findTargetClassFile(List<String> sourceRoots, String targetClass) {
        for (String sourceRoot : sourceRoots) {
            TargetClassInfo targetClassInfo = findFileRecursively(sourceRoot, targetClass);
            if (targetClassInfo != null) {
                return targetClassInfo;
            }
        }
        return null;
    }

    private static TargetClassInfo findFileRecursively(String directoryPath, String targetClass) {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return null;
        }

        // Determine if the targetClass is a fully qualified name
        String targetFileName = targetClass.contains(".") ?
                targetClass.replace('.', File.separatorChar) + ".java" :
                targetClass + ".java";

        try (Stream<Path> pathStream = Files.walk(directory)) {
            List<Path> files = pathStream.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path file : files) {
                if (file.toString().endsWith(targetFileName)) {
                    Path directoryParentPath = file.getParent();
                    String parentPathString = directoryParentPath.toString();
                    String separator = File.separator;
                    String srcMainJava = "src" + separator + "main" + separator + "java";
                    int indexToSrcMainJava = parentPathString.indexOf(srcMainJava);
                    if (indexToSrcMainJava != -1) {
                        String sourceRootPath = parentPathString.substring(0, indexToSrcMainJava);
                        return new TargetClassInfo(file, Paths.get(sourceRootPath));
                    }
                }
            }
        } catch (IOException e) {
            // Handle exception appropriately, maybe log it
            e.printStackTrace();
        }
        return null;
    }
}

