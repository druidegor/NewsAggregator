@file:OptIn(ExperimentalTime::class)

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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class NewsRepositoryImpl(
    private val apiService: NewsApi,
    private val newsDao: NewsDao
) : NewsRepository {

    override fun observeArticles(topic: String): Flow<List<Article>> {
        return newsDao.observeArticles(topic).map { list -> list.map { it.toDomain() }}
    }

    override suspend fun refreshArticles(topic: String): DataResult<Unit> {
        return try {
            val result = if (topic == "headlines") {
                apiService.getTopHeadlines()
            } else {
                apiService.getNewsByQuery(topic)
            }
            val currentTime = Clock.System.now()

            newsDao.addArticles(result.articles.map { it.toDbModel(topic,currentTime) })
            DataResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("NewsRepository").e(e) { "Data is not refreshed" }
            DataResult.Error(e)
        }
    }

    override suspend fun addSubscription(topic: String): DataResult<Unit> {
        return try {
            newsDao.addSubscription(SubscriptionDbModel(topic))
            refreshArticles(topic)
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

}
