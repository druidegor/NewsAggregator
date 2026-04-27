package org.newsagg.project.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.data.network.model.NewsResponseDto

class NewsApiImpl(
    private val client: HttpClient
): NewsApi {

    override suspend fun getNewsByQuery(query: String): NewsResponseDto {

       return client.get("everything") {
            parameter("q", query)
            parameter("language", "en")
        }.body<NewsResponseDto>()

    }

    override suspend fun getTopHeadlines(): NewsResponseDto {

        return client.get("top-headlines") {
            parameter("category", "technology")
            parameter("language", "en")
        }.body<NewsResponseDto>()
    }
}