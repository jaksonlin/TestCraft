package com.github.jaksonlin.pitestintellij.ui

import com.github.jaksonlin.pitestintellij.annotations.AnnotationFieldType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.icons.AllIcons

class CustomAnnotationCompletionLookupElement (
    private val value: String,
    private val fieldType: AnnotationFieldType,
    private val isDefaultValue: Boolean = false
) : LookupElement() {

    override fun getLookupString(): String = value

    override fun renderElement(presentation: LookupElementPresentation) {
        presentation.itemText = value
        presentation.typeText = fieldType.toString()

        // Add appropriate icon based on type
        presentation.icon = when (fieldType) {
            AnnotationFieldType.STATUS -> AllIcons.General.Settings
            AnnotationFieldType.STRING_LIST -> AllIcons.Nodes.EntryPoints
            else -> AllIcons.Nodes.Field
        }

        if (isDefaultValue) {
            presentation.isItemTextBold = true
            presentation.typeText = "Default"
        }
    }
}