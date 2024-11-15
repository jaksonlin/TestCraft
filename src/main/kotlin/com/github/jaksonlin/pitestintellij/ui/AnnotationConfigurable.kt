package com.github.jaksonlin.pitestintellij.ui

import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.github.jaksonlin.pitestintellij.annotations.AnnotationSchema
import com.intellij.json.JsonFileType
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.panels.VerticalLayout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class AnnotationConfigurable : Configurable {
    private var editor: EditorTextField? = null
    private var packageTextField: JTextField? = null
    private var autoImportCheckbox: JCheckBox? = null
    private val configService = service<AnnotationConfigService>()

    override fun getDisplayName(): String = "Test Annotation Configuration"

    override fun createComponent(): JComponent {
        val mainPanel = JBPanel<JBPanel<*>>(VerticalLayout(10))
        
        // Import Settings Section (now at top)
        mainPanel.add(createImportSettingsPanel())
        
        // Schema Editor Section
        mainPanel.add(JBLabel("Annotation Schema:"))
        mainPanel.add(createSchemaEditor())
        
        // Buttons Panel
        mainPanel.add(createButtonsPanel())
        
        // Help Panel
        mainPanel.add(createHelpPanel())

        return mainPanel
    }

    private fun createSchemaEditor(): JComponent {
        val project = ProjectManager.getInstance().defaultProject
        
        editor = EditorTextField(
            EditorFactory.getInstance().createDocument(configService.state.schemaJson),
            project,
            JsonFileType.INSTANCE,
            false,
            false
        ).apply {
            setOneLineMode(false)
            setPreferredWidth(400)
            addSettingsProvider { editor ->
                editor.settings.apply {
                    isLineNumbersShown = true
                    isWhitespacesShown = true
                    isUseSoftWraps = true
                }
            }
        }
        
        return JBScrollPane(editor)
    }

    private fun createImportSettingsPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING  // Align items on the same line
        }

        // Package Label
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JBLabel("Annotation Package:"), gbc)

        // Package TextField
        packageTextField = JTextField(configService.getAnnotationPackage()).apply {
            preferredSize = Dimension(200, preferredSize.height)
        }
        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(packageTextField, gbc)

        // Auto Import Checkbox
        autoImportCheckbox = JCheckBox("Auto Import", configService.isAutoImport())
        gbc.gridx = 2
        gbc.weightx = 0.0
        gbc.insets.left = 20  // Add some space between textfield and checkbox
        panel.add(autoImportCheckbox, gbc)

        return panel
    }

    private fun createButtonsPanel(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JButton("Restore Defaults").apply {
                addActionListener {
                    editor?.text = AnnotationSchema.DEFAULT_SCHEMA
                    packageTextField?.text = "com.example.unittest.annotations"
                    autoImportCheckbox?.isSelected = true
                }
            })
        }
    }

    private fun createHelpPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(JBLabel("""
                Define your test annotation schema in JSON format.
                Available field types: STRING, STRING_LIST, STATUS
                
                Package: The base package for your annotations
                Auto Import: Automatically add import statements when generating annotations
            """.trimIndent()), BorderLayout.CENTER)
        }
    }

    override fun isModified(): Boolean {
        return editor?.text != configService.state.schemaJson ||
               packageTextField?.text != configService.getAnnotationPackage() ||
               autoImportCheckbox?.isSelected != configService.isAutoImport()
    }

    override fun apply() {
        val jsonText = editor?.text ?: return
        try {
            // Validate JSON format and schema
            val schema = Json.decodeFromString<AnnotationSchema>(jsonText)
            configService.state.schemaJson = jsonText
            
            // Update import settings
            packageTextField?.text?.let { configService.setAnnotationPackage(it) }
            autoImportCheckbox?.isSelected?.let { configService.setAutoImport(it) }
        } catch (e: Exception) {
            throw ConfigurationException("Invalid JSON schema: ${e.message}")
        }
    }

    override fun reset() {
        editor?.text = configService.state.schemaJson
        packageTextField?.text = configService.getAnnotationPackage()
        autoImportCheckbox?.isSelected = configService.isAutoImport()
    }

    override fun disposeUIResources() {
        editor = null
        packageTextField = null
        autoImportCheckbox = null
    }
}