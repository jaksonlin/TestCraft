package com.github.jaksonlin.testcraft.commands.pitest;

import com.github.jaksonlin.testcraft.context.PitestContext;
import com.intellij.openapi.project.Project;

public class StoreHistoryCommand extends PitestCommand {

    public StoreHistoryCommand(Project project, PitestContext context) {
        super(project, context);
    }

    @Override
    public void execute() {
        runHistoryManager.saveRunHistory(getContext());
    }
}
