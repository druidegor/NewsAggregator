package org.newsagg.project.data.mapper

import org.newsagg.project.data.network.model.ArticleDto
import org.newsagg.project.domain.model.Article
import kotlin.time.Clock
import kotlin.time.Instant

fun ArticleDto.toDomain(): Article {
    return Article(
        title = title ?: "",
        description = description ?: "",
        publishedAt = publishedAt?.toTimestamp() ?: 0,
        sourceName = source?.name ?: "",
        url = url ?: "",
        imageUrl = urlToImage
    )
}

fun String.toTimestamp(): Long {
    return try {
        val instant = Instant.parse(this)
        instant.toEpochMilliseconds()
    } catch (e: Exception) {
        Clock.System.now().toEpochMilliseconds()
    }
}