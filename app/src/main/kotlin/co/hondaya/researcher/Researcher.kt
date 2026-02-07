package co.hondaya.researcher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResearchKeyPoint(
    val text: String,
    val sources: List<String> = emptyList()
)

@Serializable
data class ResearchItem(
    val url: String,
    val title: String? = null,
    val snippet: String? = null,
    @SerialName("key_points")
    val keyPoints: List<ResearchKeyPoint> = emptyList(),
    val sources: List<String> = emptyList()
)

@Serializable
data class ResearchResult(
    val items: List<ResearchItem>,
    val notes: String? = null
)

interface Researcher {
    fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult
}
