package co.hondaya.researcher

class DeepResearcher : Researcher {
    override fun research(topic: String, timeWindow: String, maxItems: Int): ResearchResult {
        return ResearchResult(
            items = emptyList(),
            notes = "Search is disabled (SearchGateway removed)."
        )
    }
}
