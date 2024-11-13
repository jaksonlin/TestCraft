package com.github.jaksonlin.pitestintellij.completion

import com.github.jaksonlin.pitestintellij.annotations.*
import com.github.jaksonlin.pitestintellij.services.AnnotationConfigService
import com.github.jaksonlin.pitestintellij.testutil.TestBase
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.LogLevel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement

class AnnotationCompletionTest : LightJavaCodeInsightFixtureTestCase(), TestBase {
    
    private lateinit var configService: AnnotationConfigService

    override fun setUp() {
        super.setUp()
        setupServices(getTestRootDisposable()) // Pass the disposable from parent class
        
        // Add the UnitTest annotation class
        myFixture.addClass("""
            public @interface UnitTest {
                String status() default "TODO";
                String[] testPoints() default {};
            }
        """.trimIndent())

        // Get the registered service
        configService = project.getService(AnnotationConfigService::class.java)
        
        // Set up test schema
        val testSchema = AnnotationSchema(
            annotationClassName = "UnitTest",
            fields = listOf(
                AnnotationFieldConfig(
                    name = "status",
                    type = AnnotationFieldType.STATUS,
                    validation = FieldValidation(
                        validValues = listOf("TODO", "IN_PROGRESS", "DONE"),
                        allowCustomValues = false,
                        mode = ValidationMode.EXACT
                    ),
                    defaultValue = DefaultValue.StringValue("TODO")
                ),
                AnnotationFieldConfig(
                    name = "testPoints",
                    type = AnnotationFieldType.STRING_LIST,
                    validation = FieldValidation(
                        validValues = listOf(
                            "Boundary Value",
                            "Error Handling",
                            "Performance",
                            "Security"
                        ),
                        allowCustomValues = true,
                        mode = ValidationMode.CONTAINS
                    ),
                    defaultValue = DefaultValue.StringListValue(emptyList())
                )
            )
        )
        
        configService.updateSchema(testSchema)
    }




    fun testStatusCompletion() {
        myFixture.configureByText("TestClass.java", """
            @UnitTest(status = "<caret>")
            public class TestClass {
            }
        """.trimIndent())

        // Type the opening quote and a character to trigger completion
        myFixture.type("T")
        myFixture.completeBasic()
        
        // Get all lookup elements
        val lookupElements = myFixture.lookupElements
        assertNotNull("Should have completions", lookupElements)
        
        val completions = lookupElements!!.map { it.lookupString }
        println("Available completions: ${completions.joinToString()}")
    
        assertTrue("Should contain TODO", completions.contains("TODO"))
        assertTrue("Should contain at least one completion", completions.isNotEmpty())
    }

    fun testTestPointsCompletion() {
        myFixture.configureByText("TestClass.java", """
            @UnitTest(testPoints = {"<caret>"})
            public class TestClass {
            }
        """.trimIndent())

        val completions = myFixture.completeBasic()
        assertNotNull(completions)
        assertTrue(completions.any { it.lookupString == "Boundary Value" })
        assertTrue(completions.any { it.lookupString == "Error Handling" })
        assertTrue(completions.any { it.lookupString == "Performance" })
        assertTrue(completions.any { it.lookupString == "Security" })
    }

    fun testNoCompletionForUnknownAnnotation() {
        myFixture.configureByText("TestClass.java", """
            @SomeOtherAnnotation(value = "<caret>")
            public class TestClass {
            }
        """.trimIndent())

        val completions = myFixture.completeBasic()
        assertTrue(completions == null || completions.none { 
            it.lookupString in listOf("TODO", "DONE", "Boundary Value") 
        })
    }

    fun testNoCompletionForUnknownField() {
        myFixture.configureByText("TestClass.java", """
            @UnitTest(unknownField = "<caret>")
            public class TestClass {
            }
        """.trimIndent())

        val completions = myFixture.completeBasic()
        assertTrue(completions == null || completions.none { 
            it.lookupString in listOf("TODO", "DONE", "Boundary Value") 
        })
    }

    fun testServiceSetup() {
        // Verify service is properly registered
        val service = project.getService(AnnotationConfigService::class.java)
        assertNotNull("AnnotationConfigService should be registered", service)
        
        // Verify schema is properly loaded
        val schema = service.getSchema()
        assertEquals("UnitTest", schema.annotationClassName)
        // store the schema in a variable

        
        // Verify fields are properly configured
        val statusField = schema.fields.find { it.name == "status" }
        assertNotNull("Status field should exist", statusField)
        assertEquals(AnnotationFieldType.STATUS, statusField!!.type)
        
        val testPointsField = schema.fields.find { it.name == "testPoints" }
        assertNotNull("TestPoints field should exist", testPointsField)
        assertEquals(AnnotationFieldType.STRING_LIST, testPointsField!!.type)
    }

    fun testCompletionDebug() {
        val positions = listOf(
            """
        @UnitTest(status = <caret>)
        """,
            """
        @UnitTest(status = "<caret>")
        """,
            """
        @UnitTest(status = "T<caret>")
        """
        )
        // set the log level to debug
        Logger.getInstance(AnnotationCompletionContributor::class.java).setLevel(LogLevel.DEBUG)

        positions.forEachIndexed { index, position ->
            println("\nTesting position $index")
            myFixture.configureByText("TestClass${index}.java", """
            ${position.trimIndent()}
            public class TestClass {
            }
        """.trimIndent())

            // Try completion
            val completions = myFixture.completeBasic()
            println("Completions at position $index: ${completions?.size ?: 0}")
            completions?.forEach {
                println("  ${it.lookupString}")
            }
        }
    }

    private fun printPsiTree(element: PsiElement, indent: String = "") {
        println("$indent${element.javaClass.simpleName}: '${element.text}'")
        element.children.forEach { child ->
            printPsiTree(child, "$indent  ")
        }
    }
}
