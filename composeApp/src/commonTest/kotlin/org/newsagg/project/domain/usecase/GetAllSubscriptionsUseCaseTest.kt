package org.newsagg.project.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllSubscriptionsUseCaseTest {

    @Test
    fun getAllSubscriptionsUseCase_shouldReturnSubscriptionsFromRepository() = runTest {
        val newsDao = FakeNewsDao()
        val newsApi = FakeNewsApi()
        val repository = NewsRepositoryImpl(newsApi, newsDao)
        val useCase = GetAllSubscriptionsUseCase(repository)

        val expectedSubscriptions = listOf(
            SubscriptionDbModel(topic = "kotlin"),
            SubscriptionDbModel(topic = "android")
        )

        newsDao.subscriptions.value = expectedSubscriptions

        useCase().test {
            val actualSubscriptions = awaitItem()

            assertEquals(2, actualSubscriptions.size)
            assertEquals("kotlin", actualSubscriptions[0])
            assertEquals("android", actualSubscriptions[1])

            cancelAndIgnoreRemainingEvents()
        }
    }

}