package co.hondaya

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.notifier.WebhookNotifier
import co.hondaya.orchestrator.Orchestrator
import co.hondaya.publisher.FilePublisher
import co.hondaya.researcher.DeepResearcher
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
        val outputPath = System.getenv("BULKNEWS_OUTPUT")?.takeIf { it.isNotBlank() }
            ?: error("Environment variable BULKNEWS_OUTPUT is required.")

        val inputJson = File(inputPath).readText(StandardCharsets.UTF_8)
        val runContext = Json.decodeFromString<RunContext>(inputJson)

        val topicSummaries = orchestrator.run(runContext)
        val outputJson = Json.encodeToString<List<TopicSummary>>(topicSummaries)

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(outputJson, StandardCharsets.UTF_8)
    }

    companion object {
        fun defaultOrchestrator(): Orchestrator {
            return Orchestrator(
                researcher = DeepResearcher(),
                summarizer = SimpleSummarizer(),
                publisher = FilePublisher(),
                notifier = WebhookNotifier()
            )
        }
    }
}
