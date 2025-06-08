package com.github.jaksonlin.testcraft.infrastructure.commands.pitest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jaksonlin.testcraft.domain.context.PitestContext;
import com.github.jaksonlin.testcraft.presentation.components.common.ItemSelectionComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.github.jaksonlin.testcraft.util.ClassFileInfo;
import com.github.jaksonlin.testcraft.util.JavaFileProcessor;

public class MethodToMutateCommand extends PitestCommand {
    private final JavaFileProcessor javaFileProcessor = new JavaFileProcessor();
    public MethodToMutateCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        List<String> methods = getTargetClassMethods();
        if (methods == null || methods.isEmpty()) {
            return;
        }
        getContext().setMethodsToMutate(String.join(",", methods));
    }

    private List<String> getTargetClassMethods() {
        try {
            Optional<ClassFileInfo> classFileInfo = javaFileProcessor.getFullyQualifiedName(getContext().getTargetClassFilePath());
            if (!classFileInfo.isPresent()) {
                showError("Cannot get fully qualified name for target class");
                throw new IllegalStateException("Cannot get fully qualified name for target class");
            }
            List<String> methods = classFileInfo.get().getMethods();
            ItemSelectionComponent<String> itemSelectionComponent = new ItemSelectionComponent<String>(getProject(), "Select the methods to mutate");
            itemSelectionComponent.setItems(methods);
            AtomicReference<List<String>> result = new AtomicReference<>();
            ApplicationManager.getApplication().invokeAndWait(() -> {
                itemSelectionComponent.showDialog();
                result.set(itemSelectionComponent.getSelectedItems());
            }, ModalityState.defaultModalityState());
            // format into QualifiedClassName::methodName
            return result.get().stream()
                .map(method -> classFileInfo.get().getFullyQualifiedName() + "::" + method)
                .collect(Collectors.toList());
        } catch(IOException ex){
            showError("Error getting fully qualified name for target class: " + ex.getMessage());
            throw new IllegalStateException("Error getting fully qualified name for target class", ex);
        }
        
    }

}
