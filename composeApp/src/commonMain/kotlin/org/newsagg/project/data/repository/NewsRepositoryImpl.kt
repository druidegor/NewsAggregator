package org.newsagg.project.data.repository

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.mapper.toDbModel
import org.newsagg.project.data.mapper.toDomain
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository
import kotlinx.coroutines.flow.map

class NewsRepositoryImpl(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao
): NewsRepository {


    override suspend fun getTopHeadlines(): List<Article> {
        return newsApi.getTopHeadlines().articles.map { it.toDomain() }
    }

    override suspend fun searchNewsByQuery(query: String): List<Article> {
        return newsApi.getNewsByQuery(query).articles.map { it.toDomain() }
    }
}