package org.newsagg.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.repository.NewsRepository

class GetAllSubscriptionsUseCase(
    private val newsRepository: NewsRepository
) {

    operator fun invoke(): Flow<List<String>> {
        return newsRepository.getAllSubscriptions()
    }
}