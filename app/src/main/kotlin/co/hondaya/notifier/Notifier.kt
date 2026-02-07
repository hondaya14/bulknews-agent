package co.hondaya.notifier

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary

interface Notifier {
    fun notify(
        context: RunContext,
        summaries: List<TopicSummary>
    )
}
