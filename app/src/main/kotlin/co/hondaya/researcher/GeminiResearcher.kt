package co.hondaya.researcher

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import kotlinx.coroutines.runBlocking

class GeminiResearcher : Researcher {
    private val apiKey: String = System.getenv("GOOGLE_API_KEY")
        ?: error("Environment variable GOOGLE_API_KEY is required for GeminiResearcher.")
    
    private val systemPrompt: String by lazy {
        loadSystemPrompt()
    }

    override fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult {
        return runBlocking {
            try {
                val agent = AIAgent(
                    promptExecutor = simpleGoogleAIExecutor(apiKey),
                    llmModel = GoogleModels.Gemini2_0Flash,
                    systemPrompt = systemPrompt,
                    temperature = 0.3
                )

                val userPrompt = buildUserPrompt(topic, timeWindow, maxItems)
                val response = agent.run(userPrompt)

                parseResearchResponse(response)
            } catch (e: Exception) {
                ResearchResult(
                    items = emptyList(),
                    notes = "Research failed: ${e.message}"
                )
            }
        }
    }

    private fun loadSystemPrompt(): String {
        return this::class.java.classLoader
            .getResourceAsStream("prompts/system_prompt.md")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Could not load system prompt from resources")
    }

    private fun buildUserPrompt(topic: String, timeWindow: String, maxItems: Int): String {
        return """
            Search for $maxItems relevant articles about: "$topic"
            Time window: $timeWindow
            
            Find high-quality, recent sources and format them as described in the system prompt.
        """.trimIndent()
    }

    private fun parseResearchResponse(response: String): ResearchResult {
        val items = mutableListOf<ResearchItem>()
        val entries = response.split("---").map { it.trim() }.filter { it.isNotBlank() }

        for (entry in entries) {
            val lines = entry.lines()
            var url: String? = null
            var title: String? = null
            val snippetLines = mutableListOf<String>()
            var currentField: String? = null

            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.isBlank()) continue

                when {
                    trimmedLine.startsWith("URL:", ignoreCase = true) -> {
                        url = trimmedLine.substringAfter("URL:").trim().takeIf { it.isNotBlank() }
                        currentField = "URL"
                    }
                    trimmedLine.startsWith("TITLE:", ignoreCase = true) -> {
                        title = trimmedLine.substringAfter("TITLE:").trim().takeIf { it.isNotBlank() }
                        currentField = "TITLE"
                    }
                    trimmedLine.startsWith("SNIPPET:", ignoreCase = true) -> {
                        val snippetStart = trimmedLine.substringAfter("SNIPPET:").trim()
                        if (snippetStart.isNotBlank()) {
                            snippetLines.add(snippetStart)
                        }
                        currentField = "SNIPPET"
                    }
                    currentField == "SNIPPET" -> {
                        // Continuation of snippet on next line
                        snippetLines.add(trimmedLine)
                    }
                }
            }

            if (url != null) {
                items.add(
                    ResearchItem(
                        url = url,
                        title = title,
                        snippet = snippetLines.joinToString(" ").takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        return ResearchResult(
            items = items,
            notes = if (items.isEmpty()) "No items found in response" else null
        )
    }
}
