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
              "required": true,
              "defaultValue": {"type": "NullValue"},
              "validation": {
                "allowEmpty": false
              }
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
              "name": "lastUpdateTime",
              "type": "STRING",
              "required": true,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "lastUpdateAuthor",
              "type": "STRING",
              "required": true,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "methodSignature",
              "type": "STRING",
              "required": true,
              "defaultValue": {"type": "NullValue"}
            },
            {
              "name": "testPoints",
              "type": "STRING_LIST",
              "required": true,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              },
              "validation": {
                "validValues": [
                  "Boundary Value",
                  "Equivalence Class",
                  "Error Handling",
                  "Performance",
                  "Security",
                  "Integration",
                  "Edge Case"
                ],
                "allowCustomValues": true,
                "minLength": 1,
                "mode": "CONTAINS",
                "allowEmpty": false
              }
            },
            {
              "name": "status",
              "type": "STATUS",
              "required": true,
              "defaultValue": {
                "type": "StringValue",
                "value": "TODO"
              },
              "validation": {
                "validValues": [
                  "TODO",
                  "IN_PROGRESS",
                  "DONE",
                  "DEPRECATED",
                  "BROKEN"
                ],
                "allowCustomValues": false,
                "mode": "CONTAINS",
                "allowEmpty": false
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
              },
              "validation": {
                "minLength": 1
              }
            },
            {
              "name": "relatedRequirements",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              },
              "validation": {
                "minLength": 1
              }
            },
            {
              "name": "relatedTestCases",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              },
              "validation": {
                "minLength": 1
              }
            },
            {
              "name": "relatedDefects",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": {
                "type": "StringListValue",
                "value": []
              },
              "validation": {
                "minLength": 1
              }
            }
          ]
        }
        """.trimIndent()
    }
}