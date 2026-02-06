package co.hondaya.summarizer

import co.hondaya.model.ArticleSummary
import co.hondaya.model.KeyPoint
import co.hondaya.researcher.ResearchItem

class SimpleSummarizer : Summarizer {
    override fun summarize(
        topic: String,
        timeWindow: String,
        items: List<ResearchItem>
    ): List<ArticleSummary> {
        if (items.isEmpty()) {
            return listOf(failSafeArticle())
        }

        return items.map { item ->
            val title = item.title ?: item.url
            val tldr = buildTldr(item)
            val keyPoints = buildKeyPoints(item)
            ArticleSummary(
                title = title,
                tldr = tldr.ifEmpty { listOf(failSafeLine()) },
                keyPoints = keyPoints,
                whyItMatters = null,
                sources = listOf(item.url)
            )
        }
    }

    private fun buildTldr(item: ResearchItem): List<String> {
        val snippet = item.snippet?.trim().orEmpty()
        return if (snippet.isBlank()) emptyList() else listOf(snippet).take(2)
    }

    private fun buildKeyPoints(item: ResearchItem): List<KeyPoint> {
        val candidates = item.snippet
            ?.let { splitSentences(it) }
            .orEmpty()

        return candidates
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
            .map { KeyPoint(text = it, sources = listOf(item.url)) }
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
