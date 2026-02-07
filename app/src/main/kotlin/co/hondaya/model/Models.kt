package co.hondaya.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunContext(
    val topics: List<String>,
    @SerialName("time_window")
    val timeWindow: String,
    @SerialName("max_items_per_topic")
    val maxItemsPerTopic: Int
)

@Serializable
data class TopicSummary(
    val topic: String,
    val summary: String,
    val articles: List<ArticleSummary>
)

@Serializable
data class ArticleSummary(
    val title: String,
    val tldr: List<String>,
    @SerialName("key_points")
    val keyPoints: List<KeyPoint>,
    @SerialName("why_it_matters")
    val whyItMatters: String? = null,
    val sources: List<String>
)

@Serializable
data class KeyPoint(
    val text: String,
    val sources: List<String>
)
