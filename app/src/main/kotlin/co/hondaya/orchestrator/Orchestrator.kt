package co.hondaya.orchestrator

import co.hondaya.researcher.Researcher
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.notifier.Notifier
import co.hondaya.publisher.Publisher
import co.hondaya.summarizer.Summarizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

class Orchestrator(
    private val researcher: Researcher,
    private val summarizer: Summarizer,
    private val publisher: Publisher,
    private val notifier: Notifier
) {
    fun run(context: RunContext): List<TopicSummary> = runBlocking {
        val summaries = coroutineScope {
            context.topics.map { topic ->
                async {
                    val research = researcher.research(topic, context.timeWindow, context.maxItemsPerTopic)
                    val items = research.items.distinctBy { it.url }.take(context.maxItemsPerTopic)
                    val articles = summarizer.summarize(topic, context.timeWindow, items)
                    TopicSummary(
                        topic = topic,
                        timeWindow = context.timeWindow,
                        articles = articles
                    )
                }
            }.awaitAll()
        }

        val publishResult = publisher.publish(context, summaries)
        notifier.notify(context, summaries, publishResult)
        summaries
    }
}
