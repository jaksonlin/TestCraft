package com.github.jaksonlin.pitestintellij.ui

import AnnotationConfigService
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
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class AnnotationConfigurable : Configurable {
    private var editor: EditorTextField? = null
    private val configService = service<AnnotationConfigService>()

    override fun getDisplayName(): String = "Test Annotation Configuration"

    override fun createComponent(): JComponent {
        val project = ProjectManager.getInstance().defaultProject
        val editorFactory = EditorFactory.getInstance()

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

        val mainPanel = JBPanel<JBPanel<*>>(VerticalLayout(10))
        
        // Add editor with scroll pane
        mainPanel.add(JBScrollPane(editor))
        
        // Add buttons panel
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val restoreDefaultsButton = JButton("Restore Defaults").apply {
            addActionListener {
                editor?.text = AnnotationSchema.DEFAULT_SCHEMA
            }
        }
        buttonsPanel.add(restoreDefaultsButton)
        mainPanel.add(buttonsPanel)
        
        // Add help panel
        mainPanel.add(createHelpPanel())

        return mainPanel
    }

    private fun createHelpPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(
                JBLabel("""
                Define your test annotation schema in JSON format.
                Available field types: STRING, STRING_LIST, STATUS
                Example schema is shown by default.
            """.trimIndent()), BorderLayout.CENTER)
        }
    }

    override fun isModified(): Boolean {
        return editor?.text != configService.state.schemaJson
    }

    override fun apply() {
        val jsonText = editor?.text ?: return
        try {
            // Validate JSON format and schema
            val schema = Json.decodeFromString<AnnotationSchema>(jsonText)
            configService.state.schemaJson = jsonText
        } catch (e: Exception) {
            throw ConfigurationException("Invalid JSON schema: ${e.message}")
        }
    }

    override fun reset() {
        editor?.text = configService.state.schemaJson
    }

    override fun disposeUIResources() {
        editor = null
    }
}