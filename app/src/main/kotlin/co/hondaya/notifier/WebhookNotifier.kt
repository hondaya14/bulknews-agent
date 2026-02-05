package co.hondaya.notifier

import co.hondaya.model.JsonSupport
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class WebhookNotifier(
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10))
        .build(),
    private val url: String = resolveUrl(),
    private val disabled: Boolean = false
) : Notifier {
    override fun notify(
        context: RunContext,
        summaries: List<TopicSummary>,
        publishResult: PublishResult
    ) {
        if (disabled) {
            return
        }

        val payload = SlackWebhookPayload(
            text = buildMessage(context, summaries, publishResult)
        )
        val json = JsonSupport.instance.encodeToString(SlackWebhookPayload.serializer(), payload)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build()

        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    companion object {
        private fun resolveUrl(): String {
            return System.getenv("BULKNEWS_WEBHOOK_URL")?.takeIf { it.isNotBlank() }
                ?: error("Unset BULKNEWS_WEBHOOK_URL")
        }
    }

    private fun buildMessage(
        context: RunContext,
        summaries: List<TopicSummary>,
        publishResult: PublishResult
    ): String {
        val topicLines = summaries
            .sortedBy { it.topic }
            .joinToString(separator = "\n") { "- ${it.topic} (${it.articles.size} articles)" }

        return buildString {
            appendLine("Bulknews run completed.")
            appendLine("Time window: ${context.timeWindow}")
            appendLine("Topics: ${summaries.size}")
            appendLine()
            appendLine("Outputs:")
            appendLine("- dir: ${publishResult.outputDir}")
            appendLine("- json: ${publishResult.jsonPath}")
            appendLine("- md: ${publishResult.markdownPath}")
            appendLine()
            appendLine("Topic breakdown:")
            appendLine(topicLines.ifBlank { "- (none)" })
        }.trimEnd()
    }
}

@Serializable
private data class SlackWebhookPayload(
    val text: String
)
