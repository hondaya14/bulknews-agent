package co.hondaya.researcher

import kotlinx.serialization.Serializable

@Serializable
data class ResearchItem(
    val url: String,
    val title: String? = null,
    val snippet: String? = null
)

@Serializable
data class ResearchResult(
    val items: List<ResearchItem>,
    val notes: String? = null
)

interface Researcher {
    fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult
}
