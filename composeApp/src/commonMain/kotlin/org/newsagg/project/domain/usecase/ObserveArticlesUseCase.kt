package org.newsagg.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class ObserveArticlesUseCase(
    private val newsRepository: NewsRepository
) {

    operator fun invoke(topic: String): Flow<List<Article>> {
        return newsRepository.observeArticles(topic)
    }
}