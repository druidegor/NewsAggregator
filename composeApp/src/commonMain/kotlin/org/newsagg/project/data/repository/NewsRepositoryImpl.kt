package org.newsagg.project.data.repository

import org.newsagg.project.data.mapper.toDomain
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val newsApi: NewsApi
): NewsRepository {

    override suspend fun getNews(query: String, language: String): List<Article> {
        return newsApi.getNews(query, language).articles.map {
            it.toDomain()
        }
    }
}