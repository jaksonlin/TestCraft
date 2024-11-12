package com.github.jaksonlin.pitestintellij.annotations


data class AnnotationSchema(
    val annotationClassName: String,
    val fields: List<AnnotationFieldConfig>
) {
    companion object {
        // Default schema matching UnittestCaseInfoContext
        val DEFAULT_SCHEMA = """
        {
          "annotationClassName": "UnittestCaseInfo",
          "fields": [
            {
              "name": "author",
              "type": "STRING",
              "required": true
            },
            {
              "name": "title",
              "type": "STRING",
              "required": true
            },
            {
              "name": "targetClass",
              "type": "STRING",
              "required": true
            },
            {
              "name": "targetMethod",
              "type": "STRING",
              "required": true
            },
            {
              "name": "testPoints",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": []
            },
            {
              "name": "status",
              "type": "STATUS",
              "required": false,
              "defaultValue": "TODO"
            },
            {
              "name": "description",
              "type": "STRING",
              "required": false
            },
            {
              "name": "tags",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": []
            },
            {
              "name": "relatedRequirements",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": []
            },
            {
              "name": "relatedDefects",
              "type": "STRING_LIST",
              "required": false,
              "defaultValue": []
            }
          ]
        }
        """.trimIndent()
    }
}