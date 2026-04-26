package org.newsagg.project.domain.model

data class Article(
    val title: String,
    val description: String,
    val publishedAt: Long,
    val sourceName: String,
    val url: String,
    val imageUrl: String?
)
