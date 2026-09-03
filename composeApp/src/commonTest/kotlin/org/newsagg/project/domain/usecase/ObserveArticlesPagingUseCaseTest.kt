package org.newsagg.project.domain.usecase

import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.domain.model.Article
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class ObserveArticlesPagingUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun observeUseCase_shouldProxyCallToRepository() = runTest {

        val newsDao = FakeNewsDao()
        val newsApi = FakeNewsApi()
        val fakeRepository = NewsRepositoryImpl(newsApi,newsDao)
        val useCase = ObserveArticlesPagingUseCase(fakeRepository)
        val expectedTopic = "headlines"

        newsDao.articles.value = listOf(
            ArticleDbModel(
                title = "Android Studio",
                description = "New features available",
                publishedAt = Clock.System.now(),
                sourceName = "Google",
                url = "https://developer.android.com",
                imageUrl = null,
                topic = expectedTopic,
                cachedAt = Clock.System.now()
            )
        )

        val items = useCase(expectedTopic).asSnapshot()
        assertEquals(expected = 1, actual = newsDao.observeCallCount)
        assertEquals(expected = 1, actual = items.size)
    }
}