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
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
            },
            {
              "name": "title",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
            },
            {
              "name": "targetClass",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
            },
            {
              "name": "targetMethod",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
            },
            {
              "name": "lastUpdateTime",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
            },
            {
              "name": "lastUpdateAuthor",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
            },
            {
              "name": "methodSignature",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
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
              "type": "STRING",
              "required": false,
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
                "allowCustomValues": true,
                "mode": "CONTAINS",
                "allowEmpty": false
              }
            },
            {
              "name": "description",
              "type": "STRING",
              "required": true,
              "validation": {
                "allowEmpty": false
              },
              "defaultValue": {
                "type": "StringValue",
                "value": ""
              }
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