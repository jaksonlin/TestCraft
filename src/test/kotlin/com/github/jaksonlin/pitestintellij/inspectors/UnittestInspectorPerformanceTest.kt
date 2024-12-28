package com.github.jaksonlin.pitestintellij.inspectors
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.junit.Test

class UnittestInspectorPerformanceTest : LightJavaCodeInsightFixtureTestCase() {
    
    @Test
    fun `test inspection performance`() {
        // Create a large test file
        val testFileContent = buildString {
            append("""
                import org.junit.Test;
                
                public class LargeTest {
            """.trimIndent())
            
            // Add 1000 test methods
            for (i in 1..1000) {
                append("""
                    @Test
                    public void test$i() {
                        // Some test code
                    }
                """.trimIndent())
            }
            
            append("}")
        }

        myFixture.configureByText("LargeTest.java", testFileContent)
        
        // Measure performance
        val startTime = System.nanoTime()
        myFixture.enableInspections(UnittestInspector::class.java)
        myFixture.doHighlighting()
        val endTime = System.nanoTime()
        
        val durationMs = (endTime - startTime) / 1_000_000.0
        println("Inspection took $durationMs ms")
        
        // Assert reasonable performance (adjust threshold as needed)
        assertTrue("Inspection took too long: $durationMs ms", durationMs < 3000)
    }
}
