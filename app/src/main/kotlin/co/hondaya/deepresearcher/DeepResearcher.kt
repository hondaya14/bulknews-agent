package co.hondaya.deepresearcher

data class ResearchItem(
    val url: String,
    val title: String? = null,
    val snippet: String? = null
)

data class ResearchResult(
    val items: List<ResearchItem>,
    val notes: String? = null
)

interface DeepResearcher {
    fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult
}
