package co.hondaya.notifier

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary

internal object NotificationMessageFormatter {
    fun build(context: RunContext, summaries: List<TopicSummary>): String {
        return buildString {

            if (summaries.isEmpty()) {
                appendLine()
                appendLine("_No topic summaries generated._")
                return@buildString
            }

            summaries
                .sortedBy { it.topic }
                .forEach { topicSummary ->
                    appendLine()
                    appendLine("*Topic:* `${topicSummary.topic}`")
                    //appendLine("*Summary:* ${topicSummary.summary}")

                    if (topicSummary.articles.isEmpty()) {
                        appendLine("- No articles")
                        return@forEach
                    }

                    topicSummary.articles.forEachIndexed { index, article ->
                        appendLine("*${index + 1}. ${article.title}*")
                        appendLine("  tl;dr")
                        appendLine("```")
                        if (article.tldr.isEmpty()) { appendLine("N/A")
                        } else { article.tldr.forEach { line -> appendLine(line) } }
                        appendLine("```")

                        appendLine("  *Key Points*")
                        if (article.keyPoints.isEmpty()) { appendLine("")
                        } else {
                            article.keyPoints.forEach { keyPoint ->
                                appendLine("    - ${keyPoint.text}")
                                // This source is same all.
//                                appendLine("      ref: ${formatSources(keyPoint.sources)}")
                            }
                        }

                        if (!article.whyItMatters.isNullOrBlank()) {
                            appendLine("  *Why it matters*")
                            appendLine(article.whyItMatters)
                        }

                        appendLine("  *Sources*")
                        if (article.sources.isEmpty()) { appendLine("N/A")
                        } else {
                            article.sources.forEach { source -> appendLine("   :mag: ${formatUrl(source)}") }
                        }
                        appendLine("${repeat("-----", 10)}")
                    }
                }
        }.trimEnd()
    }

    private fun formatSources(sources: List<String>): String {
        return if (sources.isEmpty()) "(none)" else sources.joinToString(", ") { formatUrl(it) }
    }

    private fun formatUrl(url: String): String {
        val normalized = url.trim()
        return if (normalized.isEmpty()) "(invalid url)" else "<$normalized>"
    }
}
