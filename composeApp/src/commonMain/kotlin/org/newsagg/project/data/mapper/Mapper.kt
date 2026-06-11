@file:OptIn(ExperimentalTime::class)

package org.newsagg.project.data.mapper

import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.network.model.ArticleDto
import org.newsagg.project.domain.model.Article
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun ArticleDto.toDomain(): Article {
    return Article(
        title = title ?: "",
        description = description ?: "",
        publishedAt = publishedAt?.toInstantTimestamp() ?: Clock.System.now(),
        sourceName = source?.name ?: "",
        url = url ?: "",
        imageUrl = urlToImage
    )
}

@OptIn(ExperimentalTime::class)
fun String.toInstantTimestamp(): Instant{
    return try {
        Instant.parse(this)
    } catch (e: Exception) {
        Clock.System.now()
    }
}


fun String.toLongTimestamp(): Long {
    return try {
        val instant = Instant.parse(this)
        instant.toEpochMilliseconds()
    } catch (e: Exception) {
        Clock.System.now().toEpochMilliseconds()
    }
}
fun ArticleDto.toDbModel(topic: String): ArticleDbModel {
    return ArticleDbModel(
        title = title ?: "",
        description = description ?: "",
        publishedAt = publishedAt?.toLongTimestamp() ?: 0,
        sourceName = source?.name ?: "",
        url = url ?: "",
        imageUrl = urlToImage,
        topic = topic
    )
}

fun ArticleDbModel.toDomain(): Article {
    return Article(
        title = title,
        description = description,
        publishedAt = Instant.fromEpochMilliseconds(publishedAt),
        sourceName = sourceName,
        url = url,
        imageUrl = imageUrl
    )
}
