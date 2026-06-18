@file:OptIn(ExperimentalTime::class)

package org.newsagg.project.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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

    @OptIn(ExperimentalPagingApi::class)
    override fun observeArticlesPaging(topic: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = NewsRemoteMediator(
                topic = topic,
                newsDao = newsDao,
                apiService = apiService
            ),
            pagingSourceFactory = { newsDao.pagingSource(topic)}
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
    override suspend fun refreshArticles(topic: String): DataResult<Unit> {
        return try {
            newsDao.addSubscription(SubscriptionDbModel(topic))
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
