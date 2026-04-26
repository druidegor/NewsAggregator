package org.newsagg.project.data.network.api

import org.newsagg.project.data.network.model.NewsResponseDto

interface NewsApi {
    suspend fun getNews(query: String, language: String): NewsResponseDto
}