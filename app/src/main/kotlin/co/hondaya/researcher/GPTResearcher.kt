package co.hondaya.researcher

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.text.text
import kotlinx.coroutines.runBlocking

class GPTResearcher : Researcher {
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: error("Environment variable OPENAI_API_KEY is required for GPTResearcher.")

    private val systemPrompt: String by lazy { loadSystemPrompt() }

    override fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult {
        return runBlocking {
            try {
                val executor = simpleOpenAIExecutor(apiKey)

                val strategy = strategy<String, ResearchResult>("research") {
                    val prepareRequest by node<String, String> { userPrompt ->
                        text { +userPrompt }
                    }

                    val getStructuredResult by nodeLLMRequestStructured<ResearchResult>()

                    nodeStart then prepareRequest then getStructuredResult
                    edge(getStructuredResult forwardTo nodeFinish transformed { it.getOrThrow().data })
                }

                val agentConfig = AIAgentConfig(
                    prompt = prompt("research") { system(systemPrompt) }
                        .withParams(LLMParams(temperature = 0.3)),
                    model = OpenAIModels.Chat.GPT5_2,
                    maxAgentIterations = 5
                )

                val agent = AIAgent<String, ResearchResult>(
                    promptExecutor = executor,
                    strategy = strategy,
                    agentConfig = agentConfig
                )

                val userPrompt = buildUserPrompt(topic, timeWindow, maxItems)
                agent.run(userPrompt)
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
            Research topic: "$topic"
            Time window: $timeWindow
            Max items: $maxItems

            Return strict JSON only (no markdown, no prose outside JSON).
            Output schema:
            {
              "items": [
                {
                  "url": "https://...",
                  "title": "string or null",
                  "snippet": "short factual summary or null",
                  "key_points": [
                    {
                      "text": "verifiable factual point; if inferred, prefix with 推測: / 考察: / 可能性:",
                      "sources": ["https://source-url"]
                    }
                  ],
                  "sources": ["https://source-url"]
                }
              ],
              "notes": "optional constraints/errors"
            }

            Rules:
            - Include at most $maxItems items.
            - Every key point must include at least one source URL.
            - Prefer primary sources (official blog, release notes, RFC, spec, paper).
            - Do not include unverifiable claims.
        """.trimIndent()
    }
}
