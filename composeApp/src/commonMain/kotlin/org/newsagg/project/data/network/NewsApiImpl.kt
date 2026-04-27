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

    override suspend fun getNews(query: String, language: String): NewsResponseDto {

       val response = client.get("everything") {
            parameter("q", query)
            parameter("language", language)
        }.body<NewsResponseDto>()

        println("DEBUG: Received ${response.articles.size} articles")

        return response
    }
}