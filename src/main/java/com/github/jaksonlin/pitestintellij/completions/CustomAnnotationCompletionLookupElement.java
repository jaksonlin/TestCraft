package com.github.jaksonlin.pitestintellij.ui;

import com.github.jaksonlin.pitestintellij.annotations.AnnotationFieldType;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

public class CustomAnnotationCompletionLookupElement extends LookupElement {
    private final String value;
    private final AnnotationFieldType fieldType;
    private final boolean isDefaultValue;

    public CustomAnnotationCompletionLookupElement(String value, AnnotationFieldType fieldType) {
        this(value, fieldType, false);
    }

    public CustomAnnotationCompletionLookupElement(String value, AnnotationFieldType fieldType, boolean isDefaultValue) {
        this.value = value;
        this.fieldType = fieldType;
        this.isDefaultValue = isDefaultValue;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return value;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(value);
        presentation.setTypeText(fieldType.toString());

        // Add appropriate icon based on type
        if (fieldType == AnnotationFieldType.STRING_LIST) {
            presentation.setIcon(AllIcons.Nodes.EntryPoints);
        } else {
            presentation.setIcon(AllIcons.Nodes.Field);
        }

        if (isDefaultValue) {
            presentation.setItemTextBold(true);
            presentation.setTypeText("Default");
        }
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context) {
        int startOffset = context.getStartOffset();
        int tailOffset = context.getTailOffset();

        // Insert quotes around the value
        context.getDocument().insertString(startOffset, "\"");
        context.getDocument().insertString(tailOffset + 1, "\"");

        // Move caret after the closing quote
        context.getEditor().getCaretModel().moveToOffset(tailOffset + 2);
    }
}