package co.hondaya.publisher

import co.hondaya.model.JsonSupport
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.nio.charset.StandardCharsets

class FilePublisher(
    private val outputDir: String = resolveOutputDir()
) : Publisher {
    override fun publish(context: RunContext, summaries: List<TopicSummary>): PublishResult {
        val dir = File(outputDir)
        dir.mkdirs()

        val jsonPath = File(dir, "topic_summaries.json").path
        val markdownPath = File(dir, "topic_summaries.md").path

        val jsonPayload = JsonSupport.instance.encodeToString(
            ListSerializer(TopicSummary.serializer()),
            summaries
        )
        File(jsonPath).writeText(jsonPayload, StandardCharsets.UTF_8)
        File(markdownPath).writeText(toMarkdown(context, summaries), StandardCharsets.UTF_8)

        return PublishResult(
            outputDir = dir.path,
            jsonPath = jsonPath,
            markdownPath = markdownPath
        )
    }

    private fun toMarkdown(context: RunContext, summaries: List<TopicSummary>): String {
        val builder = StringBuilder()
        builder.appendLine("# Topic Summaries")
        builder.appendLine()
        builder.appendLine("- Time window: ${context.timeWindow}")
        builder.appendLine()

        summaries.forEach { topicSummary ->
            builder.appendLine("## ${topicSummary.topic}")
            builder.appendLine()
            topicSummary.articles.forEach { article ->
                builder.appendLine("### ${article.title}")
                builder.appendLine()
                builder.appendLine("**TL;DR**")
                article.tldr.forEach { line ->
                    builder.appendLine("- $line")
                }
                builder.appendLine()
                builder.appendLine("**Key Points**")
                if (article.keyPoints.isEmpty()) {
                    builder.appendLine("- (no verified primary sources)")
                } else {
                    article.keyPoints.forEach { point ->
                        val sources = point.sources.joinToString(", ")
                        builder.appendLine("- ${point.text}  ")
                        builder.appendLine("  Source: $sources")
                    }
                }
                if (!article.whyItMatters.isNullOrBlank()) {
                    builder.appendLine()
                    builder.appendLine("**Why it matters**")
                    builder.appendLine(article.whyItMatters)
                }
                builder.appendLine()
                builder.appendLine("**Sources**")
                if (article.sources.isEmpty()) {
                    builder.appendLine("- (none)")
                } else {
                    article.sources.forEach { source ->
                        builder.appendLine("- $source")
                    }
                }
                builder.appendLine()
            }
        }
        return builder.toString()
    }

    companion object {
        private fun resolveOutputDir(): String {
            return System.getenv("BULKNEWS_OUTPUT_DIR")?.takeIf { it.isNotBlank() }
                ?: "build/output"
        }
    }
}
