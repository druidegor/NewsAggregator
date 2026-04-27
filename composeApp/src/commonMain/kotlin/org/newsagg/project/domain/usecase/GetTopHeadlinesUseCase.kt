package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class GetTopHeadlinesUseCase(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(): List<Article> {
        return newsRepository.getTopHeadlines()
    }
}