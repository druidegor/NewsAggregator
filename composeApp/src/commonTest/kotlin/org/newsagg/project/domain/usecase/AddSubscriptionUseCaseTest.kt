package org.newsagg.project.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals

class AddSubscriptionUseCaseTest {

    @Test
    fun addSubscriptionUseCase_shouldAddSubscriptionToDb() = runTest {

        val newsDao = FakeNewsDao()
        val newsApi = FakeNewsApi()
        val fakeRepository = NewsRepositoryImpl(newsApi, newsDao)
        val useCase = AddSubscriptionUseCase(fakeRepository)

        val topic = "kotlin"
        useCase(topic)

        val subscriptions = fakeRepository.getAllSubscriptions().first()

        assertEquals(1, subscriptions.size)
        assertEquals(topic, subscriptions.first())
    }
}