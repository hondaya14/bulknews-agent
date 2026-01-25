package com.bulknews.agent.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Double = 0.7,
    val topK: Int = 40,
    val topP: Double = 0.95,
    val maxOutputTokens: Int = 2048
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null
)

class GeminiClient(private val apiKey: String) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun generateContent(prompt: String, model: String = "gemini-pro"): String {
        logger.info { "Generating content with Gemini API" }
        
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 2048
            )
        )

        val response = httpClient.post("$baseUrl/$model:generateContent") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status.isSuccess()) {
            val geminiResponse: GeminiResponse = response.body()
            val generatedText = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No content generated")
            logger.info { "Successfully generated content" }
            return generatedText
        } else {
            val error = response.body<String>()
            logger.error { "Gemini API error: $error" }
            throw Exception("Failed to generate content: ${response.status}")
        }
    }

    fun close() {
        httpClient.close()
    }
}
