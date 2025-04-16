package com.github.jaksonlin.pitestintellij.annotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class AnnotationSchema {
    private String annotationClassName;
    private List<AnnotationFieldConfig> fields;
    public static final String DEFAULT_SCHEMA = """
                {
                  "annotationClassName": "UnittestCaseInfo",
                  "fields": [
                    {
                      "name": "author",
                      "type": "STRING",
                      "required": true,
                      "valueProvider": {
                        "type": "FIRST_CREATOR_AUTHOR"
                      },
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
                      "valueProvider": {
                        "type": "METHOD_NAME_BASED"
                      },
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
                      "valueProvider": {
                        "type": "CLASS_NAME"
                      },
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
                      "valueProvider": {
                        "type": "METHOD_NAME"
                      },
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
                      "valueProvider": {
                        "type": "LAST_MODIFIER_TIME",
                        "format": "yyyy-MM-dd HH:mm:ss"
                      },
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
                      "valueProvider": {
                        "type": "LAST_MODIFIER_AUTHOR"
                      },
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
                      "valueProvider": {
                        "type": "METHOD_SIGNATURE"
                      },
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
                      "required": false,
                      "valueProvider": {
                        "type": "FIXED_VALUE",
                        "value": ["Functionality"]
                      },
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
                        "mode": "CONTAINS",
                        "allowEmpty": true
                      }
                    },
                    {
                      "name": "status",
                      "type": "STRING",
                      "required": false,
                      "valueProvider": {
                        "type": "FIXED_VALUE",
                        "value": "TODO"
                      },
                      "defaultValue": {
                        "type": "StringValue",
                        "value": "TODO"
                      },
                      "validation": {
                        "validValues": [
                          "TODO",
                          "IN_PROGRESS",
                          "DONE",
                          "BLOCKED"
                        ],
                        "allowCustomValues": false,
                        "mode": "EXACT",
                        "allowEmpty": false
                      }
                    },
                    {
                      "name": "description",
                      "type": "STRING",
                      "required": false,
                      "valueProvider": {
                        "type": "METHOD_NAME_BASED"
                      },
                      "validation": {
                        "allowEmpty": true
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
                        "allowEmpty": true
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
                        "allowEmpty": true
                      }
                    },
                    {
                      "name": "relatedTestcases",
                      "type": "STRING_LIST",
                      "required": false,
                      "defaultValue": {
                        "type": "StringListValue",
                        "value": []
                      },
                      "validation": {
                        "allowEmpty": true
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
                        "allowEmpty": true
                      }
                    }
                  ]
                }
                """;

    public AnnotationSchema() {
    }

    public AnnotationSchema(String annotationClassName, List<AnnotationFieldConfig> fields) {
        this.annotationClassName = annotationClassName;
        this.fields = fields;
    }

    public String getAnnotationClassName() {
        return annotationClassName;
    }

    public void setAnnotationClassName(String annotationClassName) {
        this.annotationClassName = annotationClassName;
    }

    public List<AnnotationFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<AnnotationFieldConfig> fields) {
        this.fields = fields;
    }

    public static class Companion {
        private static final ObjectMapper json;

        static {
            json = new ObjectMapper();
        }

        @Nullable
        public static AnnotationSchema fromJson(@NotNull String jsonString) {
            try {
                return json.readValue(jsonString, AnnotationSchema.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}