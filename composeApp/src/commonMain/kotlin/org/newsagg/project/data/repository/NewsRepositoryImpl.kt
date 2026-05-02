package org.newsagg.project.data.repository

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.mapper.toDbModel
import org.newsagg.project.data.mapper.toDomain
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository
import kotlinx.coroutines.flow.map
import org.newsagg.project.data.local.model.ArticleDbModel

class NewsRepositoryImpl(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao
): NewsRepository {

    override fun getAllSubscriptions(): Flow<List<String>> {
        return newsDao.getAllSubscriptions().map { subscriptions ->
            subscriptions.map { it.topic }
        }
    }

    override suspend fun addSubscription(topic: String) {
        newsDao.addSubscription(topic.toDbModel())
        val articles = loadArticles(topic)
        newsDao.addArticles(articles)
    }

    override suspend fun deleteSubscription(topic: String) {
        newsDao.deleteSubscription(topic.toDbModel())
    }

    override fun getArticlesByTopics(topics: List<String>): Flow<List<Article>> {
        return newsDao.getArticlesByTopic(topics).map { articles ->
            articles.map { it.toDomain() }
        }
    }

    private suspend fun loadArticles(topic: String): List<ArticleDbModel> {
        return try {
            newsApi.getNewsByQuery(topic).articles.map { it.toDbModel(topic) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTopHeadlines(): List<Article> {
        return newsApi.getTopHeadlines().articles.map { it.toDomain() }
    }

}