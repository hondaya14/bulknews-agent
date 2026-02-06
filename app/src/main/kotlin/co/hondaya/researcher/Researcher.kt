package co.hondaya.researcher

data class ResearchItem(
    val url: String,
    val title: String? = null,
    val snippet: String? = null
)

data class ResearchResult(
    val items: List<ResearchItem>,
    val notes: String? = null
)

interface Researcher {
    fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult
}
