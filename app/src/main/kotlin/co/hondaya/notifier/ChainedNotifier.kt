package co.hondaya.notifier

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary
import co.hondaya.publisher.PublishResult

class ChainedNotifier(
    private val notifiers: List<Notifier>
) : Notifier {
    override fun notify(
        context: RunContext,
        summaries: List<TopicSummary>,
        publishResult: PublishResult
    ) {
        notifiers.forEach { notifier ->
            notifier.notify(context, summaries, publishResult)
        }
    }
}
