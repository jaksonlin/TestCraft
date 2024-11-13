package com.github.jaksonlin.pitestintellij.annotations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class AnnotationValidatorTest {

    class ListLengthValidation {
        @Test
        fun `should validate minimum list length`() {
            val schema = AnnotationSchema(
                annotationClassName = "Test",
                fields = listOf(
                    AnnotationFieldConfig(
                        name = "testPoints",
                        type = AnnotationFieldType.STRING_LIST,
                        validation = FieldValidation(
                            minLength = 2
                        )
                    )
                )
            )
            
            val validator = AnnotationValidator(schema)
            
            // Test invalid case (too few elements)
            val resultInvalid = validator.validate(mapOf(
                "testPoints" to listOf("point1")
            ))
            assertTrue(resultInvalid is AnnotationValidator.ValidationResult.Invalid)
            assertEquals(
                "testPoints must contain at least 2 elements",
                (resultInvalid as AnnotationValidator.ValidationResult.Invalid).errors.first()
            )

            // Test valid case
            val resultValid = validator.validate(mapOf(
                "testPoints" to listOf("point1", "point2")
            ))
            assertTrue(resultValid is AnnotationValidator.ValidationResult.Valid)
        }

        @Test
        fun `should validate maximum list length`() {
            val schema = AnnotationSchema(
                annotationClassName = "Test",
                fields = listOf(
                    AnnotationFieldConfig(
                        name = "tags",
                        type = AnnotationFieldType.STRING_LIST,
                        validation = FieldValidation(
                            maxLength = 3
                        )
                    )
                )
            )
            
            val validator = AnnotationValidator(schema)
            
            // Test invalid case (too many elements)
            val resultInvalid = validator.validate(mapOf(
                "tags" to listOf("tag1", "tag2", "tag3", "tag4")
            ))
            assertTrue(resultInvalid is AnnotationValidator.ValidationResult.Invalid)
            assertEquals(
                "tags cannot contain more than 3 elements",
                (resultInvalid as AnnotationValidator.ValidationResult.Invalid).errors.first()
            )

            // Test valid case
            val resultValid = validator.validate(mapOf(
                "tags" to listOf("tag1", "tag2", "tag3")
            ))
            assertTrue(resultValid is AnnotationValidator.ValidationResult.Valid)
        }
    }

    class ValidValuesValidation {
        @Test
        fun `should validate string list allowed values when custom values not allowed`() {
            val schema = AnnotationSchema(
                annotationClassName = "Test",
                fields = listOf(
                    AnnotationFieldConfig(
                        name = "testPoints",
                        type = AnnotationFieldType.STRING_LIST,
                        validation = FieldValidation(
                            validValues = listOf("Valid1", "Valid2", "Valid3"),
                            allowCustomValues = false
                        )
                    )
                )
            )
            
            val validator = AnnotationValidator(schema)
            
            // Test invalid case (contains invalid value)
            val resultInvalid = validator.validate(mapOf(
                "testPoints" to listOf("Valid1", "Invalid")
            ))
            assertTrue(resultInvalid is AnnotationValidator.ValidationResult.Invalid)
            assertTrue(
                (resultInvalid as AnnotationValidator.ValidationResult.Invalid)
                    .errors.first()
                    .contains("Invalid values for testPoints: Invalid")
            )

            // Test valid case
            val resultValid = validator.validate(mapOf(
                "testPoints" to listOf("Valid1", "Valid2")
            ))
            assertTrue(resultValid is AnnotationValidator.ValidationResult.Valid)
        }

        @Test
        fun `should allow custom values when configured`() {
            val schema = AnnotationSchema(
                annotationClassName = "Test",
                fields = listOf(
                    AnnotationFieldConfig(
                        name = "testPoints",
                        type = AnnotationFieldType.STRING_LIST,
                        validation = FieldValidation(
                            validValues = listOf("Valid1", "Valid2"),
                            allowCustomValues = true
                        )
                    )
                )
            )
            
            val validator = AnnotationValidator(schema)
            
            // Test with custom value (should be valid)
            val result = validator.validate(mapOf(
                "testPoints" to listOf("Valid1", "CustomValue")
            ))
            assertTrue(result is AnnotationValidator.ValidationResult.Valid)
        }
    }

    class RequiredFieldValidation {
        @Test
        fun `should validate required fields`() {
            val schema = AnnotationSchema(
                annotationClassName = "Test",
                fields = listOf(
                    AnnotationFieldConfig(
                        name = "required",
                        type = AnnotationFieldType.STRING,
                        required = true
                    )
                )
            )
            
            val validator = AnnotationValidator(schema)
            
            // Test missing required field
            val resultMissing = validator.validate(mapOf())
            assertTrue(resultMissing is AnnotationValidator.ValidationResult.Invalid)
            assertEquals(
                "Missing required field: required",
                (resultMissing as AnnotationValidator.ValidationResult.Invalid).errors.first()
            )

            // Test with required field
            val resultValid = validator.validate(mapOf(
                "required" to "value"
            ))
            assertTrue(resultValid is AnnotationValidator.ValidationResult.Valid)
        }
    }

    class TypeValidation {
        @Test
        fun `should validate field types`() {
            val schema = AnnotationSchema(
                annotationClassName = "Test",
                fields = listOf(
                    AnnotationFieldConfig(
                        name = "stringField",
                        type = AnnotationFieldType.STRING
                    ),
                    AnnotationFieldConfig(
                        name = "listField",
                        type = AnnotationFieldType.STRING_LIST
                    )
                )
            )
            
            val validator = AnnotationValidator(schema)
            
            // Test invalid types
            val resultInvalid = validator.validate(mapOf(
                "stringField" to listOf("wrong type"),
                "listField" to "wrong type"
            ))
            assertTrue(resultInvalid is AnnotationValidator.ValidationResult.Invalid)
            assertEquals(2, (resultInvalid as AnnotationValidator.ValidationResult.Invalid).errors.size)
            
            // Test valid types
            val resultValid = validator.validate(mapOf(
                "stringField" to "correct",
                "listField" to listOf("correct")
            ))
            assertTrue(resultValid is AnnotationValidator.ValidationResult.Valid)
        }
    }
}