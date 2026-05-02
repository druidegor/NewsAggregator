package org.newsagg.project.domain.usecase

import org.newsagg.project.domain.repository.NewsRepository

class AddSubscriptionUseCase(
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(topic: String) {
        newsRepository.addSubscription(topic)
    }
}