package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class SearchNewsByQueryUseCase(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(query: String): List<Article> {
        return newsRepository.searchNewsByQuery(query = query)
    }
}