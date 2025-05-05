package com.github.jaksonlin.testcraft.infrastructure.commands.unittestannotations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.jaksonlin.testcraft.domain.annotations.AnnotationFieldConfig;
import com.github.jaksonlin.testcraft.domain.annotations.AnnotationFieldType;
import com.github.jaksonlin.testcraft.domain.annotations.AnnotationSchema;
import com.github.jaksonlin.testcraft.domain.annotations.DefaultValue;
import com.github.jaksonlin.testcraft.infrastructure.commands.testscan.UnittestCaseCheckCommand;
import com.github.jaksonlin.testcraft.domain.context.CaseCheckContext;
import com.github.jaksonlin.testcraft.infrastructure.services.config.AnnotationConfigService;
import com.github.jaksonlin.testcraft.infrastructure.services.business.AnnotationValueProviderService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckboxTreeListener;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class GenerateAnnotationCommand extends UnittestCaseCheckCommand {
    private final PsiElementFactory psiElementFactory;
    private final AnnotationConfigService configService;
    private final AnnotationValueProviderService valueProviderService;
    private static final Logger LOG = Logger.getInstance(GenerateAnnotationCommand.class);

    public GenerateAnnotationCommand(Project project, CaseCheckContext context) {
        super(project, context);
        this.psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
        this.configService = ApplicationManager.getApplication().getService(AnnotationConfigService.class);
        this.valueProviderService = project.getService(AnnotationValueProviderService.class);
    }

    @Override
    public void execute() {
        ProgressManager.getInstance().run(new Task.Backgroundable(getProject(), "Generating Annotations") {
            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);

                PsiMethod[] methods = ReadAction.compute(() -> getContext().getPsiClass().getMethods());
                
                if (methods.length == 0) {
                    ApplicationManager.getApplication().invokeLater(() -> showNoMethodMessage(getProject()));
                    return;
                }
                generateAnnotationForSelectedMethod(indicator);
            }
        });
    }

    private void generateAnnotationForSelectedMethod(ProgressIndicator indicator) {
        List<PsiMethod> testMethods = ReadAction.compute(() -> {
            PsiClass psiClass = getContext().getPsiClass();
            PsiMethod[] allMethods = psiClass.getMethods();
            return Arrays.stream(allMethods)
                    .filter(this::canAddAnnotation)
                    .collect(Collectors.toList());
        });

        if (testMethods.isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> showNoTestMethodCanAddMessage(getProject()));
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            String[] methodNames = testMethods.stream().map(PsiMethod::getName).toArray(String[]::new);
            boolean[] selected = new boolean[methodNames.length];
            Arrays.fill(selected, true);

            DialogWrapper dialog = new DialogWrapper(getProject()) {
                private final CheckboxTree tree = createMethodSelectionTree(methodNames, selected);
                private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                {
                    init();
                    setTitle("Select Test Methods");
                    createButtons();
                }

                private void createButtons() {
                    JButton checkAllButton = new JButton("Check All");
                    checkAllButton.addActionListener(e -> setAllNodesChecked(true));

                    JButton uncheckAllButton = new JButton("Uncheck All");
                    uncheckAllButton.addActionListener(e -> setAllNodesChecked(false));

                    buttonPanel.add(checkAllButton);
                    buttonPanel.add(uncheckAllButton);
                }

                private void setAllNodesChecked(boolean checked) {
                    CheckedTreeNode root = (CheckedTreeNode) tree.getModel().getRoot();
                    setNodeAndChildrenChecked(root, checked);
                    tree.repaint();
                }

                private void setNodeAndChildrenChecked(CheckedTreeNode node, boolean checked) {
                    node.setChecked(checked);
                    for (int i = 0; i < node.getChildCount(); i++) {
                        CheckedTreeNode child = (CheckedTreeNode) node.getChildAt(i);
                        setNodeAndChildrenChecked(child, checked);

                        if (child.getUserObject() instanceof String) {
                            String methodName = (String) child.getUserObject();
                            for (int j = 0; j < methodNames.length; j++) {
                                if (methodNames[j].equals(methodName)) {
                                    selected[j] = checked;
                                    break;
                                }
                            }
                        }
                    }
                }

                @Override
                protected JComponent createCenterPanel() {
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.setPreferredSize(new Dimension(400, 400));

                    panel.add(buttonPanel, BorderLayout.NORTH);

                    JPanel treePanel = new JPanel(new BorderLayout());
                    treePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    treePanel.add(new JBScrollPane(tree), BorderLayout.CENTER);
                    panel.add(treePanel, BorderLayout.CENTER);

                    return panel;
                }
            };

            if (dialog.showAndGet()) {
                int selectedMethodCount = 0;
                for (boolean isSelected : selected) {
                    if (isSelected) {
                        selectedMethodCount++;
                    }
                }
                if (selectedMethodCount > 0) {
                    CountDownLatch latch = new CountDownLatch(selectedMethodCount);
                    ProgressManager.getInstance().run(new Task.Backgroundable(getProject(), "Applying annotations") {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            indicator.setIndeterminate(true);
                            for (int i = 0; i < testMethods.size(); i++) {
                                if (selected[i]) {
                                    int finalI = i;
                                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                                        try {
                                            generateAnnotation(testMethods.get(finalI), getContext().getSchema());
                                        } catch (Exception e) {
                                            LOG.error("Failed to generate annotation", e);
                                        } finally {
                                            latch.countDown();
                                        }
                                    });
                                }
                            }
                            try {
                                latch.await();
                            } catch (InterruptedException e) {
                                LOG.error("Interrupted while waiting for latch", e);
                            }
                        }
                    });
                }
            }
        });
    }

    private CheckboxTree createMethodSelectionTree(String[] methodNames, boolean[] selected) {
        CheckedTreeNode root = new CheckedTreeNode("Test Methods");

        for (int i = 0; i < methodNames.length; i++) {
            CheckedTreeNode node = new CheckedTreeNode(methodNames[i]);
            node.setChecked(selected[i]);
            root.add(node);
        }

        return new CheckboxTree(new CheckboxTree.CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (value instanceof CheckedTreeNode) {
                    Object userObject = ((CheckedTreeNode) value).getUserObject();
                    if (userObject instanceof String) {
                        getTextRenderer().append((String) userObject);
                    } else {
                        getTextRenderer().append(userObject.toString());
                    }
                }
            }
        }, root) {
            {
                addCheckboxTreeListener(new CheckboxTreeListener() {
                    @Override
                    public void nodeStateChanged(CheckedTreeNode node) {
                        Object userObject = node.getUserObject();
                        if (userObject instanceof String) {
                            String methodName = (String) userObject;
                            for (int i = 0; i < methodNames.length; i++) {
                                if (methodNames[i].equals(methodName)) {
                                    selected[i] = node.isChecked();
                                    break;
                                }
                            }
                        }
                    }
                });
                setRootVisible(false);
                setShowsRootHandles(true);
            }
        };
    }

    private boolean isMethodJunitTestMethod(PsiMethod psiMethod) {
        for (PsiAnnotation annotation : psiMethod.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if ("org.junit.Test".equals(qualifiedName) || "org.junit.jupiter.api.Test".equals(qualifiedName) || "Test".equals(qualifiedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean canAddAnnotation(PsiMethod psiMethod) {
        return ReadAction.compute(() -> isMethodJunitTestMethod(psiMethod) && findTargetAnnotation(psiMethod, getContext().getSchema()) == null);
    }


    protected void generateAnnotation(PsiMethod psiMethod, AnnotationSchema schema) {
        String annotationText = ReadAction.compute(() -> {
            CaseCheckContext newContext = getContext().copy(psiMethod);
            return buildAnnotationStr(schema, newContext);
        });

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            PsiDocumentManager.getInstance(getProject()).commitDocument(PsiDocumentManager.getInstance(getProject()).getDocument(psiMethod.getContainingFile()));

            if (configService.isAutoImport()) {
                addImportIfNeeded(psiMethod, schema.getAnnotationClassName());
            }
            PsiAnnotation annotation = buildAnnotation(annotationText);
            psiMethod.getModifierList().addAfter(annotation, null);
        });
    }

    private void addImportIfNeeded(PsiMethod psiMethod, String annotationClassName) {
        PsiJavaFile file = (PsiJavaFile) psiMethod.getContainingFile();
        if (file == null) return;
        LOG.info("Processing file: " + file.getName());

        PsiImportList importList = file.getImportList();
        if (importList == null) return;
        LOG.info("Current imports: " + Arrays.stream(importList.getImportStatements()).map(statement -> statement.getQualifiedName() != null ? statement.getQualifiedName() : "null").collect(Collectors.joining(", ")));

        String qualifiedName = configService.getAnnotationPackage() + "." + annotationClassName;
        LOG.info("Trying to add import for: " + qualifiedName);

        boolean alreadyImported = Arrays.stream(importList.getImportStatements()).anyMatch(statement -> qualifiedName.equals(statement.getQualifiedName()));
        if (!alreadyImported) {
            LOG.info("Import not found, attempting to add");
            Project project = file.getProject();
            JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);

            LOG.info("Searching for class in global scope");
            PsiClass psiClass = facade.findClass(qualifiedName, scope);
            LOG.info("Found class: " + (psiClass != null));

            if (psiClass != null) {
                try {
                    PsiImportStatement importStatement = psiElementFactory.createImportStatement(psiClass);
                    LOG.info("Created import statement: " + importStatement.getText());
                    importList.add(importStatement);
                    LOG.info("Import added successfully");
                } catch (Exception e) {
                    LOG.error("Failed to add import", e);
                }
            }
        } else {
            LOG.info("Import already exists");
        }
    }

    private String buildAnnotationStr(AnnotationSchema schema, CaseCheckContext buildAnnotationContext) {
        StringBuilder annotationText = new StringBuilder();
        annotationText.append("@").append(schema.getAnnotationClassName()).append("(\n");
        List<AnnotationFieldConfig> requiredFields = schema.getFields().stream().filter(AnnotationFieldConfig::isRequired).collect(Collectors.toList());
        for (int i = 0; i < requiredFields.size(); i++) {
            AnnotationFieldConfig field = requiredFields.get(i);
            if (i > 0) annotationText.append(",\n");
            annotationText.append("    ").append(field.getName()).append(" = ");

            Object value = field.getValueProvider() != null ? valueProviderService.provideValue(field.getValueProvider(), buildAnnotationContext) : field.getDefaultValue();

            if (field.getType() == AnnotationFieldType.STRING) {
                annotationText.append("\"").append(value).append("\"");
            } else if (field.getType() == AnnotationFieldType.STRING_LIST) {
                annotationText.append("{");
                if (value instanceof ArrayNode) {
                    ArrayNode arrayNode = (ArrayNode) value;
                    if (arrayNode != null) {
                        for (int j = 0; j < arrayNode.size(); j++) {
                            String str = arrayNode.get(j).toString();
                            if (j > 0) annotationText.append(", ");
                            annotationText.append(str.startsWith("\"") && str.endsWith("\"") ? str : "\"" + str + "\"");
                        }
                    }
                } else {
                    // comes from DefaultValue, not value provider
                    if (value instanceof DefaultValue.StringListValue){
                        List<String> list = ((DefaultValue.StringListValue) value).getValue();
                        for (int j = 0; j < list.size(); j++) {
                            if (j > 0) annotationText.append(", ");
                            annotationText.append("\"").append(list.get(j)).append("\"");
                        }
                    }
                }

                annotationText.append("}");
            }
        }
        annotationText.append("\n)");
        return annotationText.toString();
    }

    private PsiAnnotation buildAnnotation(String annotationText) {
        return psiElementFactory.createAnnotationFromText(annotationText, null);
    }
}