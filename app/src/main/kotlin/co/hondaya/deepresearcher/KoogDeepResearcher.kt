package co.hondaya.deepresearcher

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import co.hondaya.model.JsonSupport
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

class KoogDeepResearcher(
    private val searchGateway: SearchGateway
) : DeepResearcher {
    override fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult {
        val apiKey = System.getenv("OPENAI_API_KEY")?.takeIf { it.isNotBlank() }
            ?: return ResearchResult(emptyList(), "OPENAI_API_KEY is not set")

        val plan = generateSearchPlan(apiKey, topic, timeWindow, maxItems)
        val queries = plan.queries.ifEmpty { listOf(topic) }
        val perQueryLimit = maxOf(1, maxItems / queries.size)

        val results = queries.flatMap { query ->
            searchGateway.search(query, perQueryLimit)
        }.distinctBy { it.url }

        val items = results.take(maxItems).map { result ->
            ResearchItem(url = result.url, title = result.title, snippet = result.snippet)
        }

        return ResearchResult(items, plan.notes)
    }

    private fun generateSearchPlan(
        apiKey: String,
        topic: String,
        timeWindow: String,
        maxItems: Int
    ): SearchPlan {
        val executor = simpleOpenAIExecutor(apiKey)
        val agent = AIAgent(
            promptExecutor = executor,
            llmModel = OpenAIModels.Chat.GPT4o,
            systemPrompt = SYSTEM_PROMPT
        )
        val userPrompt = """
            Topic: $topic
            Time window: $timeWindow
            Max items: $maxItems

            Provide a JSON object with:
            - "queries": up to 5 search queries
            - "notes": optional short notes about primary sources to prioritize
        """.trimIndent()

        val response = runBlocking { agent.run(userPrompt) }
        return parseSearchPlan(response)
    }

    private fun parseSearchPlan(response: String): SearchPlan {
        return try {
            JsonSupport.instance.decodeFromString(SearchPlan.serializer(), response)
        } catch (ex: Exception) {
            SearchPlan(queries = emptyList(), notes = "Failed to parse Koog response")
        }
    }

    companion object {
        private const val SYSTEM_PROMPT = """
You are a technical research assistant. Create search queries that prioritize primary sources such as official blogs,
release notes, RFCs, and research papers. Do not fabricate URLs. Output JSON only.
"""
    }
}

@Serializable
private data class SearchPlan(
    val queries: List<String> = emptyList(),
    val notes: String? = null
)
