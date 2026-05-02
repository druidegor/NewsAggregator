package org.newsagg.project.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "articles",
    primaryKeys = ["url", "topic"],
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionDbModel::class,
            parentColumns = ["topic"],
            childColumns = ["topic"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["topic"])]
)
data class ArticleDbModel(
    val title: String,
    val description: String,
    val publishedAt: Long,
    val sourceName: String,
    val url: String,
    val imageUrl: String?,
    val topic: String
)
