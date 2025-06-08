package com.github.jaksonlin.testcraft.infrastructure.commands.pitest;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;

import com.github.jaksonlin.testcraft.presentation.components.common.ItemSearchAdditionComponent;
import com.github.jaksonlin.testcraft.util.ClassFileInfo;
import com.github.jaksonlin.testcraft.util.FileUtils;
import com.github.jaksonlin.testcraft.util.TargetClassInfo;
import com.github.jaksonlin.testcraft.util.JavaFileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MultiTargetClassCheckCommand extends PitestCommand {
    private final JavaFileProcessor javaFileProcessor = new JavaFileProcessor();
    public MultiTargetClassCheckCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        List<String> targetClassFilePaths = getMultiTargetClass();
        if (targetClassFilePaths == null || targetClassFilePaths.isEmpty()) {
            return;
        }
        //getContext().setTargetClassFilePaths(targetClassFilePaths);
    }

    private List<String> getMultiTargetClass() {
        ItemSearchAdditionComponent<String> itemSearchAdditionComponent = new ItemSearchAdditionComponent<String>(getProject(), this::getTargetClassFullyQualifiedName, this::displayCandidateClass);
        AtomicReference<List<String>> result = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            itemSearchAdditionComponent.showDialog("Select the target class");
            result.set(itemSearchAdditionComponent.getSelectedItems());
        }, ModalityState.defaultModalityState());
        return result.get();
    }

    private List<String> getTargetClassFullyQualifiedName(String classCandidateName) {
        List<String> sourceRoots = getContext().getSourceRoots();
        TargetClassInfo targetClassInfo = FileUtils.findTargetClassFile(sourceRoots, classCandidateName);
        if (targetClassInfo == null) {
            showError("Cannot find target class file");
            throw new IllegalStateException("Cannot find target class file");
        }
        try {
            Optional<ClassFileInfo> classInfo = javaFileProcessor.getFullyQualifiedName(targetClassInfo.getFile().toString());

            if (!classInfo.isPresent()) {
                showError("Cannot get fully qualified name for target class");
                throw new IllegalStateException("Cannot get fully qualified name for target class");
            }
            List<String> fullyQualifiedNames = new ArrayList<>();
            fullyQualifiedNames.add(classInfo.get().getFullyQualifiedName());
            return fullyQualifiedNames;
        } catch (IOException e) {
            showError("Error getting fully qualified name for target class: " + e.getMessage());
            throw new IllegalStateException("Error getting fully qualified name for target class", e);
        }
    }

    private String displayCandidateClass(String classCandidateName) {
        return classCandidateName;
    }
}
