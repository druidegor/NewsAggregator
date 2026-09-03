@file:OptIn(ExperimentalTime::class)

package org.newsagg.project.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.mapper.toDomain
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository
import kotlin.time.ExperimentalTime

class NewsRepositoryImpl(
    private val apiService: NewsApi,
    private val newsDao: NewsDao
) : NewsRepository {


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
                apiService = apiService,
                newsDao = newsDao,
            ),
            pagingSourceFactory = { newsDao.pagingSource(topic)}
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun addSubscription(topic: String){
        newsDao.addSubscription(SubscriptionDbModel(topic))
    }

    override suspend fun deleteSubscription(topic: String) {
        newsDao.deleteSubscription(SubscriptionDbModel(topic))
    }

    override fun getAllSubscriptions(): Flow<List<String>> {
        return newsDao.getAllSubscriptions().map { list -> list.map { it.topic } }
    }

}
