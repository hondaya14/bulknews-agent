package co.hondaya.summarizer

import co.hondaya.researcher.ResearchItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleSummarizerTest {
    @Test
    fun `returns fail-safe article when no content is collected`() {
        val summarizer = SimpleSummarizer()
        val result = summarizer.summarize(
            topic = "Kotlin",
            timeWindow = "past 24 hours",
            items = emptyList()
        )

        assertEquals(1, result.size)
        val article = result.first()
        assertTrue(article.title.isNotBlank())
        assertTrue(article.tldr.isNotEmpty())
    }

    @Test
    fun `summarizes basic article content`() {
        val summarizer = SimpleSummarizer()
        val item = ResearchItem(
            url = "https://example.com",
            title = "Example",
            snippet = "Example description. First paragraph."
        )

        val result = summarizer.summarize(
            topic = "Example",
            timeWindow = "past 24 hours",
            items = listOf(item)
        )

        assertEquals("Example", result.first().title)
        assertTrue(result.first().keyPoints.isNotEmpty())
    }
}
