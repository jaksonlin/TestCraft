package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.Serializable

@Serializable
data class AnnotationSchema(
    val annotationClassName: String,
    val fields: List<AnnotationFieldConfig>
) {
    companion object {
        // Default schema matching UnittestCaseInfoContext
        val DEFAULT_SCHEMA = """
        {
          "annotationClassName": "Unittest",
          "fields": [
            {
              "name": "author",
              "type": "STRING",
              "required": false,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "title",
              "type": "STRING",
              "required": true,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "targetClass",
              "type": "STRING",
              "required": true,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "targetMethod",
              "type": "STRING",
              "required": true,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "testPoints",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              }
            },
            {
              "name": "status",
              "type": "STATUS",
              "required": false,
              "defaultValue": {
                "type": "StringValue",
                "value": "TODO"
              }
            },
            {
              "name": "description",
              "type": "STRING",
              "required": false,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "tags",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              }
            },
            {
              "name": "relatedRequirements",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              }
            },
            {
              "name": "relatedDefects",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              }
            }
          ]
        }
        """.trimIndent()
    }
}