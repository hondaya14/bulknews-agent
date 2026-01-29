package co.hondaya.summarizer

import co.hondaya.collector.CollectedArticle
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
            articles = emptyList()
        )

        assertEquals(1, result.size)
        val article = result.first()
        assertTrue(article.title.isNotBlank())
        assertTrue(article.tldr.isNotEmpty())
    }

    @Test
    fun `summarizes basic article content`() {
        val summarizer = SimpleSummarizer()
        val article = CollectedArticle(
            url = "https://example.com",
            title = "Example",
            description = "Example description.",
            firstParagraph = "First paragraph."
        )

        val result = summarizer.summarize(
            topic = "Example",
            timeWindow = "past 24 hours",
            articles = listOf(article)
        )

        assertEquals("Example", result.first().title)
        assertTrue(result.first().keyPoints.isNotEmpty())
    }
}
