package com.github.jaksonlin.testcraft.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
public class JavaFileProcessor {
    // Read the Java File and get the main class's fully qualified name
    public ClassFileInfo getFullyQualifiedName(String file) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file));
            String content = new String(bytes, StandardCharsets.UTF_8);
            JavaParser parser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = parser.parse(content);

            if (parseResult.isSuccessful()) {
                CompilationUnit compilationUnit = parseResult.getResult().orElse(null);
                if (compilationUnit != null) {
                    Optional<ClassOrInterfaceDeclaration> classDeclarationOptional = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class);
                    if (classDeclarationOptional.isPresent()) {
                        ClassOrInterfaceDeclaration classDeclaration = classDeclarationOptional.get();
                        String packageName = compilationUnit.getPackageDeclaration()
                                .map(PackageDeclaration::getNameAsString)
                                .orElse("");
                        String className = classDeclaration.getNameAsString();
                        String fullyQualifiedName = packageName.isEmpty() ? className : packageName + "." + className;
                        List<String> methods = classDeclaration.getMethods().stream()
                            .map(MethodDeclaration::getNameAsString)
                            .collect(Collectors.toList());
                        return new ClassFileInfo(fullyQualifiedName, className, packageName, methods);
                    }
                }
            } else {
                // Handle parsing error, maybe log it
                System.err.println("Error parsing file: " + file);
            }
        } catch (IOException e) {
            // Handle file reading error, maybe log it
            System.err.println("Error reading file: " + file);
            e.printStackTrace();
        }
        return null;
    }
}