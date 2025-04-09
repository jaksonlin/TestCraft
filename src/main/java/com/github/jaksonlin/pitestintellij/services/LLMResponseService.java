package com.github.jaksonlin.pitestintellij.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.application.ApplicationManager;

import java.awt.datatransfer.StringSelection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service(Service.Level.APP)
public final class LLMResponseService {
    private final InvalidTestCaseConfigService configService = ApplicationManager.getApplication().getService(InvalidTestCaseConfigService.class);

    /**
     * Copies the LLM response to clipboard, optionally formatting as markdown
     * @param response The raw response from LLM
     */
    public void copyResponse(String response) {
        String textToCopy = configService.isCopyAsMarkdown() ? formatAsMarkdown(response) : response;
        CopyPasteManager.getInstance().setContents(new StringSelection(textToCopy));
    }

    /**
     * Copies the prompt that would be sent to LLM
     * @param prompt The prompt that would be sent
     */
    public void copyPrompt(String prompt) {
        if (configService.isCopyPrompt()) {
            CopyPasteManager.getInstance().setContents(new StringSelection(prompt));
        }
    }

    /**
     * Formats the response as markdown, enhancing code blocks and other elements
     */
    private String formatAsMarkdown(String response) {
        // Replace code blocks with proper markdown formatting
        response = formatCodeBlocks(response);
        
        // Format bullet points
        response = formatBulletPoints(response);
        
        // Format headers
        response = formatHeaders(response);
        
        return response;
    }

    private String formatCodeBlocks(String text) {
        // Replace ```language ... ``` blocks
        Pattern codeBlockPattern = Pattern.compile("```(\\w*)\\s*\\n([\\s\\S]*?)```");
        Matcher matcher = codeBlockPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String language = matcher.group(1);
            String code = matcher.group(2);
            // Ensure code block ends with newline
            if (!code.endsWith("\n")) {
                code += "\n";
            }
            matcher.appendReplacement(sb, "```" + language + "\n" + code + "```\n");
        }
        matcher.appendTail(sb);
        
        // Replace inline code
        text = sb.toString().replaceAll("`([^`]+)`", "`$1`");
        
        return text;
    }

    private String formatBulletPoints(String text) {
        // Convert various bullet point styles to consistent markdown
        text = text.replaceAll("^•\\s", "* ");
        text = text.replaceAll("^-\\s", "* ");
        text = text.replaceAll("^\\d+\\.\\s", "1. ");
        
        return text;
    }

    private String formatHeaders(String text) {
        // Ensure headers have space after #
        text = text.replaceAll("^(#{1,6})([^\\s#])", "$1 $2");
        
        // Convert HTML headers to markdown
        text = text.replaceAll("<h1>([^<]+)</h1>", "# $1");
        text = text.replaceAll("<h2>([^<]+)</h2>", "## $1");
        text = text.replaceAll("<h3>([^<]+)</h3>", "### $1");
        
        return text;
    }
} 