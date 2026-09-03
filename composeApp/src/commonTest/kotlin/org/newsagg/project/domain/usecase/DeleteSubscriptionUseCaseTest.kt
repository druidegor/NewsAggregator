package org.newsagg.project.domain.usecase

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class DeleteSubscriptionUseCaseTest {

    @Test
    fun deleteUseCase_shouldDeleteSubscriptionFromDb() = runTest {

        val newsDao = FakeNewsDao()
        val newsApi = FakeNewsApi()
        val fakeRepository = NewsRepositoryImpl(newsApi,newsDao)
        val useCase = DeleteSubscriptionUseCase(fakeRepository)

        val topic = "kotlin"

        newsDao.articles.value = listOf(
            ArticleDbModel(
                title = "Android Studio",
                description = "New features available",
                publishedAt = Clock.System.now(),
                sourceName = "Google",
                url = "https://developer.android.com",
                imageUrl = null,
                topic = topic,
                cachedAt = Clock.System.now()
            )
        )

        useCase(topic)

        assertEquals(0, newsDao.articles.value.size)

    }
}