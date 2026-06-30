package org.newsagg.project.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_remote_keys")
data class NewsRemoteKeys(
    @PrimaryKey
    val topic: String,
    val nextPage: Int?
)