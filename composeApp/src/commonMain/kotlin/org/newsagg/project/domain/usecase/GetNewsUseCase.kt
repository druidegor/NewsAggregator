package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class GetNewsUseCase(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(query: String, language: String): List<Article> {
        return newsRepository.getNews(query, language)
    }
}