package co.hondaya.notifier

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult

interface Notifier {
    fun notify(
        context: RunContext,
        summaries: List<TopicSummary>,
        publishResult: PublishResult
    )
}
