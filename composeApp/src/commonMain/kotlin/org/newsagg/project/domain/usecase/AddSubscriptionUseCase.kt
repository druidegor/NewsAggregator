package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.repository.NewsRepository
import org.newsagg.project.util.DataResult

class AddSubscriptionUseCase(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(topic: String): DataResult<Unit> {
        return newsRepository.addSubscription(topic)
    }
}
