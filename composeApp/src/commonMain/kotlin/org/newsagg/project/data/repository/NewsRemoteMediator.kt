package org.newsagg.project.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.CancellationException
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.data.mapper.toDbModel
import kotlin.time.Clock

@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val topic: String,
    private val apiService: NewsApi,
    private val newsDao: NewsDao
): RemoteMediator<Int, ArticleDbModel>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleDbModel>
    ): MediatorResult {
        return try {
            val page = when(loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    val totalLoadedItems = state.pages.sumOf { it.data.size }
                    (totalLoadedItems / state.config.pageSize) + 1
                }
            }

            val response = if (topic == "headlines") {
                apiService.getTopHeadlines(page = page, pageSize = state.config.pageSize)
            } else {
                apiService.getNewsByQuery(query = topic, page = page, pageSize = state.config.pageSize)
            }

            val articlesDto = response.articles
            val endOfPaginationReached = articlesDto.isEmpty()

            val currentTime = Clock.System.now()
            val dbModels = articlesDto.map { it.toDbModel(topic, currentTime) }

            newsDao.updateArticlesTransaction(
                topic = topic,
                articles = dbModels,
                shouldClear = loadType == LoadType.REFRESH
            )

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}