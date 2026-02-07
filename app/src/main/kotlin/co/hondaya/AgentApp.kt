package co.hondaya

import co.hondaya.model.RunContext
import co.hondaya.notifier.WebhookNotifier
import co.hondaya.orchestrator.Orchestrator
import co.hondaya.researcher.GPTResearcher
import co.hondaya.researcher.GeminiResearcher
import co.hondaya.summarizer.SimpleSummarizer
import java.io.File
import java.nio.charset.StandardCharsets
import kotlinx.serialization.json.Json

class AgentApp(
    private val orchestrator: Orchestrator = defaultOrchestrator()
) {
    fun run() {
        val inputPath = System.getenv("BULKNEWS_INPUT")?.takeIf { it.isNotBlank() }
            ?: error("Environment variable BULKNEWS_INPUT is required.")

        val inputJson = File(inputPath).readText(StandardCharsets.UTF_8)
        val runContext = Json.decodeFromString<RunContext>(inputJson)

        orchestrator.run(runContext)
    }

    companion object {
        fun defaultOrchestrator(): Orchestrator {
            return Orchestrator(
                researcher = GeminiResearcher(),
                summarizer = SimpleSummarizer(),
                notifier = WebhookNotifier()
            )
        }
    }
}
