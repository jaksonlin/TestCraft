package com.github.jaksonlin.testcraft.infrastructure.commands.testscan;

import com.github.jaksonlin.testcraft.domain.context.CaseCheckContext;
import com.github.jaksonlin.testcraft.domain.model.InvalidTestCase;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import com.github.jaksonlin.testcraft.infrastructure.messaging.events.InvalidTestScanEvent;
import com.github.jaksonlin.testcraft.infrastructure.services.system.EventBusService;
import com.github.jaksonlin.testcraft.infrastructure.services.system.I18nService;

public class UnittestFileBatchScanCommand {
    private final Set<String> testAnnotations = new HashSet<>(Arrays.asList(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
            "Test"
    ));

    private final Project project;
    private final AnActionEvent e;

    public UnittestFileBatchScanCommand(Project project, AnActionEvent e) {
        this.project = project;
        this.e = e;
    }

    public void execute() {
        new Task.Backgroundable(project, "Scanning Test Cases", false) {
            private List<InvalidTestCase> invalidTestCases = new ArrayList<>();

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                EventBusService.getInstance().post(new InvalidTestScanEvent(InvalidTestScanEvent.INVALID_TEST_SCAN_START_EVENT, null));
                indicator.setIndeterminate(false);
                indicator.setText(I18nService.getInstance().message("testscan.scanning_test_classes"));
                
                List<PsiClass> testClasses = ReadAction.compute(() -> findTestClasses());
                if (testClasses.isEmpty()) {
                    return;
                }

                indicator.setText(I18nService.getInstance().message("testscan.checking_test_cases"));
                indicator.setFraction(0.0);
                double progressStep = 1.0 / testClasses.size();

                InspectionManager inspectionManager = InspectionManager.getInstance(project);

                for (PsiClass testClass : testClasses) {
                    if (indicator.isCanceled()) {
                        break;
                    }

                    final PsiClass finalTestClass = testClass;
                    String qualifiedName = ReadAction.compute(() -> finalTestClass.getQualifiedName());
                    indicator.setText2(I18nService.getInstance().message("testscan.checking_test_cases", qualifiedName));
                    
                    ReadAction.run(() -> {
                        for (PsiMethod method : testClass.getMethods()) {
                            if (isTestMethod(method)) {
                                CaseCheckContext caseContext = CaseCheckContext.create(method, testClass);
                                ProblemsHolder holder = new ProblemsHolder(inspectionManager, method.getContainingFile(), true);
                                UnittestFileInspectorCommand command = new UnittestFileInspectorCommand(holder, project, caseContext);
                                command.execute();

                                if (holder.hasResults()) {
                                    InvalidTestCase invalidTestCase = new InvalidTestCase(project, testClass, method);
                                    invalidTestCases.add(invalidTestCase);
                                }
                            }
                        }
                    });
                    
                    indicator.setFraction(indicator.getFraction() + progressStep);
                }
            }

            @Override
            public void onSuccess() {
                if (invalidTestCases.isEmpty()) {
                    Messages.showInfoMessage(project, I18nService.getInstance().message("testscan.no_invalid_test_cases_found"), I18nService.getInstance().message("testscan.test_case_validation_results"));
                    EventBusService.getInstance().post(new InvalidTestScanEvent(InvalidTestScanEvent.INVALID_TEST_SCAN_END_EVENT, null));
                } else {
                    StringBuilder message = new StringBuilder();
                    message.append(String.format(I18nService.getInstance().message("testscan.found_invalid_test_cases", invalidTestCases.size())));
                    for (InvalidTestCase testCase : invalidTestCases) {
                        message.append(String.format("- %s\n", testCase.getQualifiedName()));
                    }
                    if (invalidTestCases.size() > 0) {
                        // Show the tool window and select the invalid test cases tab
                        ApplicationManager.getApplication().invokeLater(() -> {
                                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("TestCraft");
                                if (toolWindow != null) {
                                    toolWindow.show();
                                    toolWindow.getContentManager().setSelectedContent(
                                        toolWindow.getContentManager().findContent(I18nService.getInstance().message("toolwindow.invalid.testcases.tab.name"))
                                    );
                                }
                            }   
                        );
                    }
                    EventBusService.getInstance().post(new InvalidTestScanEvent(InvalidTestScanEvent.INVALID_TEST_SCAN_END_EVENT, invalidTestCases));
                    Messages.showWarningDialog(project, message.toString(), I18nService.getInstance().message("testscan.test_case_validation_results"));
                    
                }
            }

            @Override
            public void onCancel() {
                Messages.showInfoMessage(project, I18nService.getInstance().message("testscan.test_case_validation_canceled"), I18nService.getInstance().message("testscan.test_case_validation_canceled"));
            }
        }.queue();
    }

    private List<PsiClass> findTestClasses() {
        List<PsiClass> testClasses = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);

        // Get the context from the action event
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (element != null) {
            // If triggered from a file or directory in the project view
            if (element instanceof PsiFile) {
                PsiFile psiFile = (PsiFile) element;
                if (psiFile.getName().endsWith(".java")) {
                    PsiClass[] classes = PsiTreeUtil.getChildrenOfType(psiFile, PsiClass.class);
                    if (classes != null) {
                        for (PsiClass psiClass : classes) {
                            if (isTestClass(psiClass)) {
                                testClasses.add(psiClass);
                            }
                        }
                    }
                }
            } else if (element instanceof PsiDirectory) {
                // If triggered from a directory, scan that directory
                findTestClassesInDirectory((PsiDirectory) element, testClasses);
            }
        } else if (file != null) {
            // If triggered from a file in the editor
            if (file.getName().endsWith(".java")) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    PsiClass[] classes = PsiTreeUtil.getChildrenOfType(psiFile, PsiClass.class);
                    if (classes != null) {
                        for (PsiClass psiClass : classes) {
                            if (isTestClass(psiClass)) {
                                testClasses.add(psiClass);
                            }
                        }
                    }
                }
            }
        } else {
            // If no specific context, scan the entire project
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(project.getBasePath()));
            if (baseDir != null) {
                VirtualFile[] children = baseDir.getChildren();
                for (VirtualFile child : children) {
                    if (child.isDirectory() && (child.getName().equals("src") || child.getName().equals("test"))) {
                        findTestClassesInDirectory(psiManager.findDirectory(child), testClasses);
                    }
                }
            }
        }

        return testClasses;
    }

    private boolean isTestClass(PsiClass psiClass) {
        // Check if class has test annotations
        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            if (annotation.getQualifiedName() != null &&
                    (annotation.getQualifiedName().contains("Test") ||
                            annotation.getQualifiedName().contains("RunWith"))) {
                return true;
            }
        }

        // Check if class has test methods
        for (PsiMethod method : psiClass.getMethods()) {
            if (isTestMethod(method)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTestMethod(PsiMethod method) {
        for (PsiAnnotation annotation : method.getAnnotations()) {
            if (annotation.getQualifiedName() != null && testAnnotations.contains(annotation.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    private void findTestClassesInDirectory(PsiDirectory directory, List<PsiClass> testClasses) {
        if (directory == null) return;

        for (PsiElement child : directory.getChildren()) {
            if (child instanceof PsiDirectory) {
                findTestClassesInDirectory((PsiDirectory) child, testClasses);
            } else if (child instanceof PsiFile) {
                PsiFile file = (PsiFile) child;
                if (file.getName().endsWith(".java")) {
                    PsiClass[] classes = PsiTreeUtil.getChildrenOfType(file, PsiClass.class);
                    if (classes != null) {
                        for (PsiClass psiClass : classes) {
                            if (isTestClass(psiClass)) {
                                testClasses.add(psiClass);
                            }
                        }
                    }
                }
            }
        }
    }
}
