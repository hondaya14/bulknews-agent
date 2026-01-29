package co.hondaya.orchestrator

import co.hondaya.collector.Collector
import co.hondaya.deepresearcher.DeepResearcher
import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.notifier.Notifier
import co.hondaya.publisher.Publisher
import co.hondaya.summarizer.Summarizer

class BatchOrchestrator(
    private val deepResearcher: DeepResearcher,
    private val collector: Collector,
    private val summarizer: Summarizer,
    private val publisher: Publisher,
    private val notifier: Notifier
) {
    fun run(context: RunContext): List<TopicSummary> {
        val summaries = context.topics.map { topic ->
            val research = deepResearcher.research(topic, context.timeWindow, context.maxItemsPerTopic)
            val researchUrls = research.items.map { it.url }
            val urls = researchUrls.distinct().take(context.maxItemsPerTopic)
            val collected = collector.collect(urls)
            val articles = summarizer.summarize(topic, context.timeWindow, collected)
            TopicSummary(
                topic = topic,
                timeWindow = context.timeWindow,
                articles = articles
            )
        }

        val publishResult = publisher.publish(context, summaries)
        notifier.notify(context, summaries, publishResult)
        return summaries
    }
}
