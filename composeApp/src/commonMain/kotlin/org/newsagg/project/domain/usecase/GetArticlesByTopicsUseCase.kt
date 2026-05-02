package org.newsagg.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class GetArticlesByTopicsUseCase(
    private val newsRepository: NewsRepository
) {

    operator fun invoke(topics: List<String>): Flow<List<Article>> {
        return newsRepository.getArticlesByTopics(topics)
    }
}