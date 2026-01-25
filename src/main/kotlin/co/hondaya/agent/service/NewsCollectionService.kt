package co.hondaya.agent.service

import co.hondaya.agent.client.GeminiClient
import co.hondaya.agent.model.NewsArticle
import co.hondaya.agent.model.NewsCollection
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

class NewsCollectionService(private val geminiClient: GeminiClient) {

    suspend fun collectNews(topics: List<String>): List<NewsCollection> = coroutineScope {
        logger.info { "Starting news collection for ${topics.size} topics" }
        
        topics.map { topic ->
            async {
                try {
                    collectNewsForTopic(topic)
                } catch (e: Exception) {
                    logger.error(e) { "Error collecting news for topic: $topic" }
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun collectNewsForTopic(topic: String): NewsCollection {
        logger.info { "Collecting news for topic: $topic" }
        
        val prompt = buildNewsCollectionPrompt(topic)
        val response = geminiClient.generateContent(prompt)
        
        val articles = parseNewsResponse(response, topic)
        
        return NewsCollection(
            topic = topic,
            articles = articles,
            generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    private fun buildNewsCollectionPrompt(topic: String): String {
        return """
            You are a news aggregation AI assistant. Please provide 5 recent news articles about "$topic".
            
            For each article, provide:
            1. Title
            2. A concise summary (2-3 sentences)
            3. Source name
            4. URL (use realistic but example URLs if you don't have real ones)
            5. Published date (use recent dates)
            
            Format your response as a JSON array with the following structure:
            [
              {
                "title": "Article Title",
                "summary": "Article summary here...",
                "source": "Source Name",
                "url": "https://example.com/article",
                "publishedDate": "2026-01-25"
              }
            ]
            
            Only return the JSON array, no additional text.
        """.trimIndent()
    }

    private fun parseNewsResponse(response: String, topic: String): List<NewsArticle> {
        return try {
            // Extract JSON from response (in case there's extra text)
            val jsonStart = response.indexOf("[")
            val jsonEnd = response.lastIndexOf("]") + 1
            
            if (jsonStart == -1 || jsonEnd == 0) {
                logger.warn { "No JSON array found in response for topic: $topic" }
                return emptyList()
            }
            
            val jsonString = response.substring(jsonStart, jsonEnd)
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<NewsArticle>>(jsonString)
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse news response for topic: $topic" }
            emptyList()
        }
    }
}
