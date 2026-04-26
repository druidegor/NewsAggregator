package org.newsagg.project.domain.repository

import org.newsagg.project.domain.model.Article

interface NewsRepository {

    suspend fun getNews(query: String, language: String): List<Article>
}
