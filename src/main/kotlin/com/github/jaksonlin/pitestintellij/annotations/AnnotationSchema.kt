package com.github.jaksonlin.pitestintellij.annotations

import kotlinx.serialization.Serializable

@Serializable
data class AnnotationSchema(
    val annotationClassName: String,
    val fields: List<AnnotationFieldConfig>
) {
    companion object {
        // Default schema matching UnittestCaseInfoContextG
        val DEFAULT_SCHEMA = """
        {
          "annotationClassName": "UnittestCaseInfo",
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
                  "BoundaryValue",
                  "NonEmpty",
                  "ErrorHandling",
                  "InputValidation",
                  "PositiveScenario",
                  "NegativeScenario",
                  "EdgeCase",
                  "Functionality",
                  "BusinessLogicValidation",
                  "BusinessInputOutput",
                  "SideEffects",
                  "StateTransition",
                  "BusinessCalculation",
                  "Security",
                  "Performance"
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