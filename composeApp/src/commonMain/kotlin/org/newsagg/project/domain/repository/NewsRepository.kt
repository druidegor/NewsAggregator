package org.newsagg.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article
import org.newsagg.project.util.DataResult

interface NewsRepository {
    suspend fun getTopHeadlines(): DataResult<List<Article>>
    suspend fun loadArticles(topic: String): DataResult<Unit>
    
    suspend fun addSubscription(topic: String): DataResult<Unit>
    suspend fun deleteSubscription(topic: String)
    fun getAllSubscriptions(): Flow<List<String>>
    fun getArticlesByTopics(topics: List<String>): Flow<List<Article>>
}
