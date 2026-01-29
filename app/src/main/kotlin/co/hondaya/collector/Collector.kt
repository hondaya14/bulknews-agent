package co.hondaya.collector

data class CollectedArticle(
    val url: String,
    val title: String?,
    val description: String?,
    val firstParagraph: String?
)

interface Collector {
    fun collect(urls: List<String>): List<CollectedArticle>
}
