package co.hondaya.publisher

import co.hondaya.model.RunContext
import co.hondaya.model.TopicSummary

data class PublishResult(
    val outputDir: String,
    val jsonPath: String,
    val markdownPath: String
)

interface Publisher {
    fun publish(context: RunContext, summaries: List<TopicSummary>): PublishResult
}
