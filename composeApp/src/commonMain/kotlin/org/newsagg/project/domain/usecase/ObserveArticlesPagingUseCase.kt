package org.newsagg.project.domain.usecase

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.repository.NewsRepository

class ObserveArticlesPagingUseCase(
    private val newsRepository: NewsRepository
) {

    operator fun invoke(topic: String): Flow<PagingData<Article>> {
        return newsRepository.observeArticlesPaging(topic)
    }
}