package org.newsagg.project.data.network.api

import org.newsagg.project.data.network.model.NewsResponseDto

interface NewsApi {
    suspend fun getNewsByQuery(query: String): NewsResponseDto

    suspend fun getTopHeadlines(): NewsResponseDto
}