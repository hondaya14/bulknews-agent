package co.hondaya.notifier

import co.hondaya.model.ArticleSummary
import co.hondaya.model.KeyPoint
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult
import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlackNotifierTest {
    @Test
    fun `skips when token is missing`() {
        val slack = mockk<Slack>(relaxed = true)
        val notifier = SlackNotifier(slack = slack, token = null, channel = "C123")

        val result = notifier.notify(sampleContext(), sampleSummaries(), samplePublishResult())

        assertEquals("slack://skipped", result.outputPath)
        verify(exactly = 0) { slack.methods(any()) }
    }

    @Test
    fun `skips when channel is missing`() {
        val slack = mockk<Slack>(relaxed = true)
        val notifier = SlackNotifier(slack = slack, token = "xoxb-token", channel = null)

        val result = notifier.notify(sampleContext(), sampleSummaries(), samplePublishResult())

        assertEquals("slack://skipped", result.outputPath)
        verify(exactly = 0) { slack.methods(any()) }
    }

    @Test
    fun `posts message and returns channel timestamp`() {
        val slack = mockk<Slack>()
        val client = mockk<MethodsClient>()
        val requestSlot = slot<ChatPostMessageRequest>()

        every { slack.methods("xoxb-token") } returns client
        every { client.chatPostMessage(capture(requestSlot)) } returns ChatPostMessageResponse().also {
            it.isOk = true
            it.ts = "1700000000.000100"
        }

        val notifier = SlackNotifier(slack = slack, token = "xoxb-token", channel = "C123")
        val result = notifier.notify(sampleContext(), sampleSummaries(), samplePublishResult())

        assertEquals("slack://C123/1700000000.000100", result.outputPath)
        assertEquals("C123", requestSlot.captured.channel)
        assertTrue(requestSlot.captured.text.contains("Bulknews run completed."))
    }

    @Test
    fun `returns error when client throws exception`() {
        val slack = mockk<Slack>()
        val client = mockk<MethodsClient>()

        every { slack.methods("xoxb-token") } returns client
        every { client.chatPostMessage(any()) } throws RuntimeException("boom")

        val notifier = SlackNotifier(slack = slack, token = "xoxb-token", channel = "C123")
        val result = notifier.notify(sampleContext(), sampleSummaries(), samplePublishResult())

        assertEquals("slack://error", result.outputPath)
    }

    private fun sampleContext(): RunContext = RunContext(
        topics = listOf("Kotlin", "Rust"),
        timeWindow = "past 24 hours",
        maxItemsPerTopic = 3
    )

    private fun sampleSummaries(): List<TopicSummary> = listOf(
        TopicSummary(
            topic = "Kotlin",
            timeWindow = "past 24 hours",
            articles = listOf(
                ArticleSummary(
                    title = "Kotlin 2.0",
                    tldr = listOf("Something changed."),
                    keyPoints = listOf(
                        KeyPoint(
                            text = "K2 is default.",
                            sources = listOf("https://example.com")
                        )
                    ),
                    whyItMatters = "Faster builds.",
                    sources = listOf("https://example.com")
                )
            )
        ),
        TopicSummary(
            topic = "Rust",
            timeWindow = "past 24 hours",
            articles = emptyList()
        )
    )

    private fun samplePublishResult(): PublishResult = PublishResult(
        outputDir = "build/output",
        jsonPath = "build/output/topic_summaries.json",
        markdownPath = "build/output/topic_summaries.md"
    )
}
