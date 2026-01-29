package co.hondaya.deepresearcher

data class SearchResult(
    val url: String,
    val title: String?,
    val snippet: String?
)

interface SearchGateway {
    fun search(query: String, limit: Int): List<SearchResult>
}
