package org.newsagg.project.data.network.api

import org.newsagg.project.data.network.model.NewsResponseDto

interface NewsApi {
    suspend fun getNewsByQuery(query: String, page: Int = 1, pageSize: Int = 1): NewsResponseDto

    suspend fun getTopHeadlines(page: Int = 1,pageSize: Int = 1): NewsResponseDto
}