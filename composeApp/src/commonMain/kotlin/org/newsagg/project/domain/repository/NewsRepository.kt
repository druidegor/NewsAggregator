package org.newsagg.project.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article
import org.newsagg.project.util.DataResult

interface NewsRepository {
    suspend fun addSubscription(topic: String): DataResult<Unit>
    suspend fun deleteSubscription(topic: String)
    fun getAllSubscriptions(): Flow<List<String>>

    fun observeArticlesPaging(topic: String): Flow<PagingData<Article>>

    fun observeArticles(topic: String): Flow<List<Article>>

    suspend fun refreshArticles (topic: String): DataResult<Unit>
}
