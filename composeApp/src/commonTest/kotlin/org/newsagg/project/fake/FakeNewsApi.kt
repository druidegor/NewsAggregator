package org.newsagg.project.fake

import kotlinx.coroutines.delay
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.data.network.model.NewsResponseDto

class FakeNewsApi: NewsApi {

    var callCount= 0
    val newsProvideResponse: () -> NewsResponseDto = { NewsResponseDto(emptyList()) }
    var shouldTrowException: Exception? = null

    override suspend fun getNewsByQuery(query: String, page: Int, pageSize: Int): NewsResponseDto {
        delay(500)
        callCount++
        shouldTrowException?.let { throw it }
        return newsProvideResponse()
    }

    override suspend fun getTopHeadlines(page: Int, pageSize: Int): NewsResponseDto {
        delay(500)
        callCount++
        shouldTrowException?.let { throw it }
        return newsProvideResponse()
    }
}