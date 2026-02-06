package co.hondaya.summarizer

import co.hondaya.model.ArticleSummary
import co.hondaya.researcher.ResearchItem

interface Summarizer {
    fun summarize(
        topic: String,
        timeWindow: String,
        items: List<ResearchItem>
    ): List<ArticleSummary>
}
