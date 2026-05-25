package org.newsagg.project.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.mapper.toDbModel
import org.newsagg.project.data.mapper.toDomain
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository
import org.newsagg.project.util.DataResult

class NewsRepositoryImpl(
    private val apiService: NewsApi,
    private val newsDao: NewsDao
) : NewsRepository {

    override suspend fun getTopHeadlines(): DataResult<List<Article>> {
        return try {
            val response = apiService.getTopHeadlines()
            val articles = response.articles.map { it.toDomain() }
            DataResult.Success(articles)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("NewsRepository").e(e) { "Data is not forced" }
            DataResult.Error(e)
        }
    }

    override suspend fun loadArticles(topic: String): DataResult<Unit>{
        return try {
            val response = apiService.getNewsByQuery(topic)
            newsDao.addArticles(response.articles.map { it.toDbModel(topic) })
            DataResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("NewsRepository").e(e) { "Data is not forced" }
            DataResult.Error(e)
        }
    }

    override suspend fun addSubscription(topic: String): DataResult<Unit> {
        return try {
            newsDao.addSubscription(SubscriptionDbModel(topic))
            loadArticles(topic)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("NewsRepository").e { "Subscriptions is not added" }
            DataResult.Error(e)
        }

    }

    override suspend fun deleteSubscription(topic: String) {
        newsDao.deleteSubscription(SubscriptionDbModel(topic))
    }

    override fun getAllSubscriptions(): Flow<List<String>> {
        return newsDao.getAllSubscriptions().map { list -> list.map { it.topic } }
    }

    override fun getArticlesByTopics(topics: List<String>): Flow<List<Article>> {
        return newsDao.getArticlesByTopic(topics).map { list -> list.map { it.toDomain() } }
    }
}
