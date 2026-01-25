package com.bulknews.agent.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsArticle(
    val title: String,
    val summary: String,
    val source: String,
    val url: String,
    val publishedDate: String
)

@Serializable
data class NewsCollection(
    val topic: String,
    val articles: List<NewsArticle>,
    val generatedAt: String
)
