package com.github.jaksonlin.pitestintellij.commands.unittestannotations

import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.github.jaksonlin.pitestintellij.annotations.DefaultValue
import com.github.jaksonlin.pitestintellij.context.CaseCheckContext
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckboxTreeListener
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class GenerateAnnotationCommand(project: Project, context: CaseCheckContext):UnittestCaseCheckCommand(project, context) {
    private val psiElementFactory: PsiElementFactory = JavaPsiFacade.getInstance(project).elementFactory
    private val configService = service<AnnotationConfigService>()

    override fun execute() {
        if (context.psiClass.methods.isEmpty()) {
            showNoMethodMessage(project)
            return
        }
        generateAnnotationForSelectedMethod()
    }

    private fun generateAnnotationForSelectedMethod() {
        val psiClass = context.psiClass
        val testMethods = psiClass.methods.filter { canAddAnnotation(it) }
        
        if (testMethods.isEmpty()) {
            showNoTestMethodCanAddMessage(project)
            return
        }
    
        val methodNames = testMethods.map { it.name }.toTypedArray()
        val selected = BooleanArray(methodNames.size) { true }
        
        val dialog = object : DialogWrapper(project) {
            private val tree: CheckboxTree = createMethodSelectionTree(methodNames, selected)
            private val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    
            init {
                init()
                title = "Select Test Methods"
                createButtons()
            }
    
            private fun createButtons() {
                val checkAllButton = JButton("Check All").apply {
                    addActionListener {
                        setAllNodesChecked(true)
                    }
                }
                
                val uncheckAllButton = JButton("Uncheck All").apply {
                    addActionListener {
                        setAllNodesChecked(false)
                    }
                }
    
                buttonPanel.add(checkAllButton)
                buttonPanel.add(uncheckAllButton)
            }
    
            private fun setAllNodesChecked(checked: Boolean) {
                val root = tree.model.root as CheckedTreeNode
                setNodeAndChildrenChecked(root, checked)
                tree.repaint()
            }
    
            private fun setNodeAndChildrenChecked(node: CheckedTreeNode, checked: Boolean) {
                node.isChecked = checked
                for (i in 0 until node.childCount) {
                    val child = node.getChildAt(i) as? CheckedTreeNode ?: continue
                    setNodeAndChildrenChecked(child, checked)
                    
                    // Update the selected array if this is a leaf node (method)
                    if (child.userObject is String) {
                        val index = methodNames.indexOf(child.userObject as String)
                        if (index >= 0) {
                            selected[index] = checked
                        }
                    }
                }
            }
    
            override fun createCenterPanel(): JComponent {
                val panel = JPanel(BorderLayout())
                panel.preferredSize = Dimension(400, 400)
                
                // Add the button panel at the top
                panel.add(buttonPanel, BorderLayout.NORTH)
                
                // Add the tree with scroll pane in the center
                val treePanel = JPanel(BorderLayout())
                treePanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                treePanel.add(JBScrollPane(tree), BorderLayout.CENTER)
                panel.add(treePanel, BorderLayout.CENTER)
                
                return panel
            }
        }
    
        if (dialog.showAndGet()) {
            // Process selected methods after OK is clicked
            testMethods.filterIndexed { index, _ -> selected[index] }
                .forEach { method ->
                    generateAnnotationForSingleMethod(method)
                }
        }
    }
    
    private fun createMethodSelectionTree(methodNames: Array<String>, selected: BooleanArray): CheckboxTree {
        val root = CheckedTreeNode("Test Methods")
        
        methodNames.forEachIndexed { index, name ->
            val node = CheckedTreeNode(name)
            node.isChecked = selected[index]
            root.add(node)
        }
    
        return CheckboxTree(
            object : CheckboxTree.CheckboxTreeCellRenderer() {
                override fun customizeRenderer(
                    tree: JTree,
                    value: Any,
                    selected: Boolean,
                    expanded: Boolean,
                    leaf: Boolean,
                    row: Int,
                    hasFocus: Boolean
                ) {
                    if (value is CheckedTreeNode) {
                        when (value.userObject) {
                            is String -> textRenderer.append(value.userObject as String)
                            else -> textRenderer.append(value.userObject.toString())
                        }
                    }
                }
            },
            root
        ).apply {
            addCheckboxTreeListener(object : CheckboxTreeListener {
                override fun nodeStateChanged(node: CheckedTreeNode) {
                    if (node.userObject is String) {
                        val index = methodNames.indexOf(node.userObject as String)
                        if (index >= 0) {
                            selected[index] = node.isChecked
                        }
                    }
                }
            })
            isRootVisible = false  // Hide the root node
            showsRootHandles = true  // Show handles for the root's children
        }
    }



    private fun isMethodJunitTestMethod(psiMethod: PsiMethod): Boolean {
        val annotations = psiMethod.annotations
        return annotations.any { it.qualifiedName == "org.junit.Test" || it.qualifiedName == "org.junit.jupiter.api.Test" || it.qualifiedName == "Test"}
    }

    private fun canAddAnnotation(psiMethod: PsiMethod): Boolean {
        if (!isMethodJunitTestMethod(psiMethod)) {
            return false
        }
        val annotation = findTargetAnnotation(psiMethod, context.schema)
        return annotation == null
    }

    private fun generateAnnotationForSingleMethod(psiMethod: PsiMethod) {
        if (!canAddAnnotation(psiMethod)) {
            return
        }
        generateAnnotation(psiMethod, context.schema)
    }

    protected fun generateAnnotation(psiMethod: PsiMethod, schema: AnnotationSchema) {
        WriteCommandAction.runWriteCommandAction(project) {
            if (configService.isAutoImport()) {
                addImportIfNeeded(psiMethod, schema.annotationClassName)
            }
            val annotation = buildAnnotation(schema)
            psiMethod.modifierList.addAfter(annotation, null)
        }
    }

    private val LOG = Logger.getInstance(GenerateAnnotationCommand::class.java)

    private fun addImportIfNeeded(psiMethod: PsiMethod, annotationClassName: String) {
        val file = psiMethod.containingFile as? PsiJavaFile ?: return
        LOG.info("Processing file: ${file.name}")
        
        val importList = file.importList ?: return
        LOG.info("Current imports: ${importList.importStatements.joinToString { it.qualifiedName ?: "null" }}")
        
        val qualifiedName = "${configService.getAnnotationPackage()}.$annotationClassName"
        LOG.info("Trying to add import for: $qualifiedName")
        
        // Only add if not already imported
        if (importList.importStatements.none { it.qualifiedName == qualifiedName }) {
            LOG.info("Import not found, attempting to add")
            
            val project = file.project
            val facade = JavaPsiFacade.getInstance(project)
            val scope = GlobalSearchScope.allScope(project)
            
            LOG.info("Searching for class in global scope")
            val psiClass = facade.findClass(qualifiedName, scope)
            LOG.info("Found class: ${psiClass != null}")
            
            try {
                val importStatement = psiElementFactory.createImportStatement(
                    psiClass ?: return
                )
                LOG.info("Created import statement: ${importStatement.text}")
                
                importList.add(importStatement)
                LOG.info("Import added successfully")
            } catch (e: Exception) {
                LOG.error("Failed to add import", e)
            }
        } else {
            LOG.info("Import already exists")
        }
    }

    private fun buildAnnotation(schema: AnnotationSchema): PsiAnnotation {
        // Build the annotation text with default values
        val requiredFields = schema.fields.filter { it.required }
        
        val fieldValues = requiredFields.joinToString(",\n    ") { field ->
            val defaultValueStr = when (val default = field.defaultValue) {
                is DefaultValue.StringValue -> "\"${default.value}\""
                is DefaultValue.StringListValue -> {
                    val values = default.value.joinToString("\", \"") { "\"$it\"" }
                    if (values.isEmpty()) "{}" else "{$values}"
                }
                is DefaultValue.NullValue -> "null"
                null -> "\"\""  // Empty string for required fields without default
            }
            "${field.name} = $defaultValueStr"
        }

        val annotationText = if (fieldValues.isEmpty()) {
            "@${schema.annotationClassName}"
        } else {
            """@${schema.annotationClassName}(
    $fieldValues
)"""
        }

        return psiElementFactory.createAnnotationFromText(annotationText, null)
    }
}