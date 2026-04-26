package org.newsagg.project.data.mapper

import org.newsagg.project.data.network.model.ArticleDto
import org.newsagg.project.domain.model.Article
import kotlin.time.Instant

fun ArticleDto.toDomain(): Article {
    return Article(
        title = title ?: "",
        description = description ?: "",
        publishedAt = parseDateToMillis(publishedAt),
        sourceName = source?.name ?: "",
        url = url,
        imageUrl = urlToImage
    )
}

fun parseDateToMillis(dateString: String): Long {
    val instant = Instant.parse(dateString)
    return instant.toEpochMilliseconds()
}