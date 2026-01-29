package co.hondaya.orchestrator

import co.hondaya.collector.Collector
import co.hondaya.deepresearcher.DeepResearcher
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
    private val deepResearcher: DeepResearcher,
    private val collector: Collector,
    private val summarizer: Summarizer,
    private val publisher: Publisher,
    private val notifier: Notifier
) {
    fun run(context: RunContext): List<TopicSummary> = runBlocking {
        val summaries = coroutineScope {
            context.topics.map { topic ->
                async {
                    val research = deepResearcher.research(topic, context.timeWindow, context.maxItemsPerTopic)
                    val urls = research.items.map { it.url }.distinct().take(context.maxItemsPerTopic)
                    val collected = collector.collect(urls)
                    val articles = summarizer.summarize(topic, context.timeWindow, collected)
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
