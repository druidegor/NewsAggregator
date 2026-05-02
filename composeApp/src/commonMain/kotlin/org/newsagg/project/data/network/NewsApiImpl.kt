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

    override suspend fun getNewsByQuery(query: String, page: Int , pageSize: Int): NewsResponseDto {

       return client.get("everything") {
            parameter("q", query)
            parameter("language", "en")
           parameter("page", page)
           parameter("pageSize", pageSize)
        }.body<NewsResponseDto>()

    }

    override suspend fun getTopHeadlines(page: Int,pageSize: Int): NewsResponseDto {

        return client.get("top-headlines") {
            parameter("category", "technology")
            parameter("language", "en")
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body<NewsResponseDto>()
    }
}