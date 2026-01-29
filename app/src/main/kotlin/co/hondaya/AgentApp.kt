package co.hondaya

import co.hondaya.collector.HttpCollector
import co.hondaya.deepresearcher.KoogDeepResearcher
import co.hondaya.deepresearcher.NoopSearchGateway
import co.hondaya.deepresearcher.SerperSearchGateway
import co.hondaya.model.JsonSupport
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.notifier.FileNotifier
import co.hondaya.orchestrator.BatchOrchestrator
import co.hondaya.publisher.FilePublisher
import co.hondaya.summarizer.SimpleSummarizer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.nio.charset.StandardCharsets

class AgentApp(
    private val orchestrator: BatchOrchestrator = defaultOrchestrator()
) {
    fun run() {
        println("Starting..")
        val inputPath = resolveInputPath()
        val outputPath = resolveOutputPath()
        val inputJson = readInput(inputPath)
        val runContext = try {
            JsonSupport.instance.decodeFromString(RunContext.serializer(), inputJson)
        } catch (ex: SerializationException) {
            System.err.println("Failed to parse input JSON: ${ex.message}")
            return
        }
        println("Parsed")

        val topicSummaries = orchestrator.run(runContext)
        val outputJson = JsonSupport.instance.encodeToString(
            ListSerializer(TopicSummary.serializer()),
            topicSummaries
        )
        writeOutput(outputPath, outputJson)
    }

    private fun readInput(inputPath: String): String {
        return File(inputPath).readText(StandardCharsets.UTF_8)
    }

    private fun writeOutput(outputPath: String, payload: String) {
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(payload, StandardCharsets.UTF_8)
    }

    companion object {
        private const val DEFAULT_INPUT = "run_context.json"
        private const val DEFAULT_OUTPUT = "build/output/topic_summaries.json"

        private fun defaultOrchestrator(): BatchOrchestrator {
            val searchGateway = createSearchGateway()
            return BatchOrchestrator(
                deepResearcher = KoogDeepResearcher(searchGateway),
                collector = HttpCollector(),
                summarizer = SimpleSummarizer(),
                publisher = FilePublisher(),
                notifier = FileNotifier()
            )
        }

        private fun createSearchGateway(): co.hondaya.deepresearcher.SearchGateway {
            val serperKey = System.getenv("SERPER_API_KEY")?.takeIf { it.isNotBlank() }
            return if (serperKey == null) {
                NoopSearchGateway()
            } else {
                SerperSearchGateway(serperKey)
            }
        }

        private fun resolveInputPath(): String {
            return System.getenv("BULKNEWS_INPUT")?.takeIf { it.isNotBlank() } ?: DEFAULT_INPUT
        }

        private fun resolveOutputPath(): String {
            return System.getenv("BULKNEWS_OUTPUT")?.takeIf { it.isNotBlank() } ?: DEFAULT_OUTPUT
        }
    }
}
