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
        publishedAt = publishedAt?.toTimestamp() ?: 0,
        sourceName = source?.name ?: "",
        url = url ?: "",
        imageUrl = urlToImage
    )
}

@OptIn(ExperimentalTime::class)
fun String.toTimestamp(): Long {
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
        publishedAt = publishedAt?.toTimestamp() ?: 0,
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
        publishedAt = publishedAt,
        sourceName = sourceName,
        url = url,
        imageUrl = imageUrl
    )
}

fun String.toDbModel(): SubscriptionDbModel {
    return SubscriptionDbModel(
        topic = this
    )
}