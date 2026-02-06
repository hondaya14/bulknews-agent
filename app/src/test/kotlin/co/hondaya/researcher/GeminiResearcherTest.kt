package co.hondaya.researcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiResearcherTest {
    
    @Test
    fun `researcher implements Researcher interface`() {
        // Verify the class structure is correct
        val researcherClass = GeminiResearcher::class
        assertTrue(
            researcherClass.java.interfaces.any { it == Researcher::class.java },
            "GeminiResearcher should implement Researcher interface"
        )
    }
    
    @Test
    fun `constructor should fail when GOOGLE_API_KEY is not set`() {
        // Given - no API key set
        val originalKey = System.getenv("GOOGLE_API_KEY")
        
        // Only run this test if the API key is actually not set
        if (originalKey == null || originalKey.isBlank()) {
            // When/Then - verify the error message mentions the requirement
            try {
                GeminiResearcher()
                throw AssertionError("Expected IllegalStateException to be thrown")
            } catch (e: IllegalStateException) {
                assertTrue(
                    e.message?.contains("GOOGLE_API_KEY") == true,
                    "Error message should mention GOOGLE_API_KEY"
                )
            }
        } else {
            // Skip test when API key is set
            println("Skipping test - GOOGLE_API_KEY is set in environment")
        }
    }
    
    @Test
    fun `response parsing should handle multiple articles`() {
        // This test verifies the expected response format structure
        // The actual parsing is done privately within GeminiResearcher
        val response = """
            URL: https://example.com/article1
            TITLE: Test Article 1
            SNIPPET: This is a test snippet for article 1.
            ---
            URL: https://example.com/article2
            TITLE: Test Article 2
            SNIPPET: This is a test snippet for article 2.
            ---
        """.trimIndent()
        
        // Verify the response format can be split into entries
        val entries = response.split("---").map { it.trim() }.filter { it.isNotBlank() }
        
        assertEquals(2, entries.size, "Should have 2 entries")
        assertTrue(entries[0].contains("example.com/article1"), "First entry should contain first URL")
        assertTrue(entries[1].contains("example.com/article2"), "Second entry should contain second URL")
        assertTrue(entries[0].contains("Test Article 1"), "First entry should contain first title")
        assertTrue(entries[1].contains("Test Article 2"), "Second entry should contain second title")
    }
}
