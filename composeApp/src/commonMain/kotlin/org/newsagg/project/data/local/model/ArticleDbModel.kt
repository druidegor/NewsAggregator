package org.newsagg.project.data.local.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "articles",
    primaryKeys = ["url", "publishedAt"],
    indices = [Index(value = ["publishedAt"])]
)
data class ArticleDbModel(
    val title: String,
    val description: String,
    val publishedAt: Long,
    val sourceName: String,
    val url: String,
    val imageUrl: String?,
)
