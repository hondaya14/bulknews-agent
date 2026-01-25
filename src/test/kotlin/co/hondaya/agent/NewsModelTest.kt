package co.hondaya.agent

import co.hondaya.agent.model.NewsArticle
import co.hondaya.agent.model.NewsCollection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NewsModelTest {

    @Test
    fun `test NewsArticle creation`() {
        val article = NewsArticle(
            title = "Test Article",
            summary = "This is a test summary",
            source = "Test Source",
            url = "https://example.com/test",
            publishedDate = "2026-01-25"
        )
        
        assertEquals("Test Article", article.title)
        assertEquals("This is a test summary", article.summary)
        assertEquals("Test Source", article.source)
        assertEquals("https://example.com/test", article.url)
        assertEquals("2026-01-25", article.publishedDate)
    }

    @Test
    fun `test NewsCollection creation`() {
        val articles = listOf(
            NewsArticle(
                title = "Article 1",
                summary = "Summary 1",
                source = "Source 1",
                url = "https://example.com/1",
                publishedDate = "2026-01-25"
            )
        )
        
        val collection = NewsCollection(
            topic = "Test Topic",
            articles = articles,
            generatedAt = "2026-01-25T12:00:00"
        )
        
        assertEquals("Test Topic", collection.topic)
        assertEquals(1, collection.articles.size)
        assertNotNull(collection.generatedAt)
    }
}
