package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.repository.NewsRepository

class DeleteSubscriptionUseCase(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(topic: String) {
        newsRepository.deleteSubscription(topic)
    }
}