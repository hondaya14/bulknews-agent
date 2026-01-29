package co.hondaya.summarizer

import co.hondaya.collector.CollectedArticle
import co.hondaya.model.ArticleSummary

interface Summarizer {
    fun summarize(
        topic: String,
        timeWindow: String,
        articles: List<CollectedArticle>
    ): List<ArticleSummary>
}
