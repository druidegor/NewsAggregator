@file:OptIn(ExperimentalTime::class)

package org.newsagg.project.data.mapper

import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.network.model.ArticleDto
import org.newsagg.project.domain.model.Article
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
private fun String.toInstantTimestamp(): Instant{
    return try {
        Instant.parse(this)
    } catch (e: Exception) {
        Clock.System.now()
    }
}

fun ArticleDto.toDbModel(topic: String, cachedAt: Instant): ArticleDbModel {
    return ArticleDbModel(
        title = title ?: "",
        description = description ?: "",
        publishedAt = publishedAt?.toInstantTimestamp() ?: Clock.System.now(),
        sourceName = source?.name ?: "",
        url = url ?: "",
        imageUrl = urlToImage,
        topic = topic,
        cachedAt = cachedAt
    )
}
fun ArticleDbModel.toDomain(): Article {
    return Article(
        title = title,
        description = description,
        publishedAt = publishedAt,
        sourceName = sourceName,
        url = url,
        imageUrl = imageUrl
    )
}
