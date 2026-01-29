package co.hondaya.summarizer

import co.hondaya.collector.CollectedArticle
import co.hondaya.model.ArticleSummary
import co.hondaya.model.KeyPoint

class SimpleSummarizer : Summarizer {
    override fun summarize(
        topic: String,
        timeWindow: String,
        articles: List<CollectedArticle>
    ): List<ArticleSummary> {
        if (articles.isEmpty()) {
            return listOf(failSafeArticle())
        }

        return articles.map { article ->
            val title = article.title ?: article.url
            val tldr = buildTldr(article)
            val keyPoints = buildKeyPoints(article)
            ArticleSummary(
                title = title,
                tldr = tldr.ifEmpty { listOf(failSafeLine()) },
                keyPoints = keyPoints,
                whyItMatters = null,
                sources = listOf(article.url)
            )
        }
    }

    private fun buildTldr(article: CollectedArticle): List<String> {
        val lines = mutableListOf<String>()
        article.description?.let { desc ->
            lines.add(desc.trim())
        }
        if (lines.size < 2) {
            article.firstParagraph?.let { para ->
                if (lines.none { it == para }) {
                    lines.add(para.trim())
                }
            }
        }
        return lines.take(2)
    }

    private fun buildKeyPoints(article: CollectedArticle): List<KeyPoint> {
        val candidates = mutableListOf<String>()
        article.description?.let { desc ->
            candidates.addAll(splitSentences(desc))
        }
        article.firstParagraph?.let { para ->
            candidates.addAll(splitSentences(para))
        }

        return candidates
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
            .map { KeyPoint(text = it, sources = listOf(article.url)) }
    }

    private fun splitSentences(text: String): List<String> {
        return text
            .split(Regex("[.!?]\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun failSafeArticle(): ArticleSummary {
        return ArticleSummary(
            title = failSafeTitle(),
            tldr = listOf(failSafeLine()),
            keyPoints = emptyList(),
            whyItMatters = null,
            sources = emptyList()
        )
    }

    private fun failSafeTitle(): String {
        return "No primary sources found"
    }

    private fun failSafeLine(): String {
        return "Unable to generate a summary because no reliable primary sources were found."
    }
}
