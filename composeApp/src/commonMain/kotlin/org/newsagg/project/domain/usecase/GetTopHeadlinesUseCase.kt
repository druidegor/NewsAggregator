package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository
import org.newsagg.project.util.DataResult

class GetTopHeadlinesUseCase(private val repository: NewsRepository) {
    suspend operator fun invoke(): DataResult<List<Article>> {
        return repository.getTopHeadlines()
    }
}
