package co.hondaya.notifier

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult
import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest

class SlackNotifier(
    private val slack: Slack = Slack.getInstance(),
    private val token: String = resolveToken(),
    private val channel: String = resolveChannel()
) : Notifier {
    override fun notify(
        context: RunContext,
        summaries: List<TopicSummary>,
        publishResult: PublishResult
    ): NotificationResult {
        val text = buildMessage(context, summaries, publishResult)
        return try {
            val client = slack.methods(token)
            val response = client.chatPostMessage(
                ChatPostMessageRequest.builder()
                    .channel(channel)
                    .text(text)
                    .build()
            )
            if (!response.isOk) {
                System.err.println("SlackNotifier failed: ${response.error}")
            }
            val ts = response.ts ?: "unknown"
            NotificationResult(outputPath = "slack://$channel/$ts")
        } catch (ex: Exception) {
            System.err.println("SlackNotifier exception: ${ex.message}")
            NotificationResult(outputPath = "slack://error")
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

    companion object {
        private fun resolveToken(): String {
            return System.getenv("BULKNEWS_SLACK_TOKEN")?.takeIf { it.isNotBlank() }
                ?: System.getenv("SLACK_BOT_TOKEN")?.takeIf { it.isNotBlank() }
                ?: error("Slack token is required. Set BULKNEWS_SLACK_TOKEN or SLACK_BOT_TOKEN.")
        }

        private fun resolveChannel(): String {
            return System.getenv("BULKNEWS_SLACK_CHANNEL")?.takeIf { it.isNotBlank() }
                ?: System.getenv("SLACK_CHANNEL")?.takeIf { it.isNotBlank() }
                ?: error("Slack channel is required. Set BULKNEWS_SLACK_CHANNEL or SLACK_CHANNEL.")
        }
    }
}
