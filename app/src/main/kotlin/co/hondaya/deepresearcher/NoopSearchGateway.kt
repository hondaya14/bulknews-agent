package co.hondaya.deepresearcher

class NoopSearchGateway : SearchGateway {
    override fun search(query: String, limit: Int): List<SearchResult> {
        return emptyList()
    }
}
