package com.github.jaksonlin.testcraft.annotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class AnnotationSchema {
    private String annotationClassName;
    private List<AnnotationFieldConfig> fields;
    public static final String DEFAULT_SCHEMA = "{\n" +
            "  \"annotationClassName\": \"UnittestCaseInfo\",\n" +
            "  \"fields\": [\n" +
            "    {\n" +
            "      \"name\": \"author\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"FIRST_CREATOR_AUTHOR\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"title\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"METHOD_NAME_BASED\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"targetClass\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"CLASS_NAME\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"targetMethod\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"METHOD_NAME\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"lastUpdateTime\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"LAST_MODIFIER_TIME\",\n" +
            "        \"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"lastUpdateAuthor\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"LAST_MODIFIER_AUTHOR\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"methodSignature\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": true,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"METHOD_SIGNATURE\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": false\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"testPoints\",\n" +
            "      \"type\": \"STRING_LIST\",\n" +
            "      \"required\": false,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"FIXED_VALUE\",\n" +
            "        \"value\": [\"Functionality\"]\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringListValue\",\n" +
            "        \"value\": []\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"validValues\": [\n" +
            "          \"BoundaryValue\",\n" +
            "          \"NonEmpty\",\n" +
            "          \"ErrorHandling\",\n" +
            "          \"InputValidation\",\n" +
            "          \"PositiveScenario\",\n" +
            "          \"NegativeScenario\",\n" +
            "          \"EdgeCase\",\n" +
            "          \"Functionality\",\n" +
            "          \"BusinessLogicValidation\",\n" +
            "          \"BusinessInputOutput\",\n" +
            "          \"SideEffects\",\n" +
            "          \"StateTransition\",\n" +
            "          \"BusinessCalculation\",\n" +
            "          \"Security\",\n" +
            "          \"Performance\"\n" +
            "        ],\n" +
            "        \"allowCustomValues\": true,\n" +
            "        \"mode\": \"CONTAINS\",\n" +
            "        \"allowEmpty\": true\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"status\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": false,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"FIXED_VALUE\",\n" +
            "        \"value\": \"TODO\"\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"TODO\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"validValues\": [\n" +
            "          \"TODO\",\n" +
            "          \"IN_PROGRESS\",\n" +
            "          \"DONE\",\n" +
            "          \"BLOCKED\"\n" +
            "        ],\n" +
            "        \"allowCustomValues\": false,\n" +
            "        \"mode\": \"EXACT\",\n" +
            "        \"allowEmpty\": false\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"description\",\n" +
            "      \"type\": \"STRING\",\n" +
            "      \"required\": false,\n" +
            "      \"valueProvider\": {\n" +
            "        \"type\": \"METHOD_NAME_BASED\"\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": true\n" +
            "      },\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringValue\",\n" +
            "        \"value\": \"\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"tags\",\n" +
            "      \"type\": \"STRING_LIST\",\n" +
            "      \"required\": false,\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringListValue\",\n" +
            "        \"value\": []\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": true\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"relatedRequirements\",\n" +
            "      \"type\": \"STRING_LIST\",\n" +
            "      \"required\": false,\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringListValue\",\n" +
            "        \"value\": []\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": true\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"relatedTestcases\",\n" +
            "      \"type\": \"STRING_LIST\",\n" +
            "      \"required\": false,\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringListValue\",\n" +
            "        \"value\": []\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": true\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"relatedDefects\",\n" +
            "      \"type\": \"STRING_LIST\",\n" +
            "      \"required\": false,\n" +
            "      \"defaultValue\": {\n" +
            "        \"type\": \"StringListValue\",\n" +
            "        \"value\": []\n" +
            "      },\n" +
            "      \"validation\": {\n" +
            "        \"allowEmpty\": true\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

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