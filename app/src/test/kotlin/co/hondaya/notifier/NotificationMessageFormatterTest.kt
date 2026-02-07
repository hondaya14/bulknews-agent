package co.hondaya.notifier

import co.hondaya.model.ArticleSummary
import co.hondaya.model.KeyPoint
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationMessageFormatterTest {
    @Test
    fun buildsSlackMrkdwnMessage() {
        val context = RunContext(
            topics = listOf("Kotlin 2.x"),
            timeWindow = "past 7 days",
            maxItemsPerTopic = 3
        )
        val summaries = listOf(
            TopicSummary(
                topic = "Kotlin <2.0> & Tooling",
                summary = "Compiler update & migration guidance",
                articles = listOf(
                    ArticleSummary(
                        title = "K2 roadmap <alpha>",
                        tldr = listOf("Default compiler changes & timelines"),
                        keyPoints = listOf(
                            KeyPoint(
                                text = "K2 becomes default in staged rollout",
                                sources = listOf("https://example.com/release-note")
                            )
                        ),
                        whyItMatters = "Build failures can increase if flags are stale.",
                        sources = listOf("https://example.com/release-note?x=1&y=2")
                    )
                )
            )
        )

        val message = NotificationMessageFormatter.build(context, summaries)

        assertTrue(message.contains("*Topic:* Kotlin &lt;2.0&gt; &amp; Tooling"))
        assertTrue(message.contains("*TL;DR*"))
        assertTrue(message.contains("*Key Points*"))
        assertTrue(message.contains("Source: <https://example.com/release-note>"))
        assertTrue(message.contains("- <https://example.com/release-note?x=1&y=2>"))
        assertFalse(message.contains("## "))
    }

    @Test
    fun showsEmptyState() {
        val context = RunContext(
            topics = emptyList(),
            timeWindow = "past 24 hours",
            maxItemsPerTopic = 3
        )

        val message = NotificationMessageFormatter.build(context, emptyList())

        assertTrue(message.contains("_No topic summaries generated._"))
    }
}
