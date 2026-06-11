@file:OptIn(ExperimentalTime::class)

package org.newsagg.project.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Article(
    val title: String,
    val description: String,
    val publishedAt: Instant,
    val sourceName: String,
    val url: String,
    val imageUrl: String?
)
