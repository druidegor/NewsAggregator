package org.newsagg.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article

interface NewsRepository {

    fun getAllSubscriptions(): Flow<List<String>>

    suspend fun addSubscription(topic: String)

    suspend fun deleteSubscription(topic: String)

    fun getArticlesByTopics(topics: List<String>): Flow<List<Article>>

    suspend fun getTopHeadlines(): List<Article>


}
