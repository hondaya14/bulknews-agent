package co.hondaya.researcher

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import kotlinx.coroutines.runBlocking

class GeminiResearcher : Researcher {
    private val apiKey: String = System.getenv("GOOGLE_API_KEY")
        ?: error("Environment variable GOOGLE_API_KEY is required for GeminiResearcher.")

    override fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult {
        return runBlocking {
            try {
                val agent = AIAgent(
                    promptExecutor = simpleGoogleAIExecutor(apiKey),
                    llmModel = GoogleModels.Gemini2_0Flash,
                    systemPrompt = buildSystemPrompt(),
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

    private fun buildSystemPrompt(): String {
        return """
            You are a technical research assistant that helps find relevant articles and resources on technical topics.
            Your task is to search for recent, reliable primary sources (official blogs, release notes, papers, specifications, RFCs).
            
            For each article you find, provide:
            1. URL - The exact URL of the source
            2. Title - The title of the article or document
            3. Snippet - A brief 1-2 sentence summary of the content
            
            Format your response as a structured list with clear separation between items.
            Use this exact format for each item:
            
            URL: [url here]
            TITLE: [title here]
            SNIPPET: [snippet here]
            ---
            
            Only include reliable, verifiable sources. Prioritize official documentation and primary sources.
        """.trimIndent()
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
