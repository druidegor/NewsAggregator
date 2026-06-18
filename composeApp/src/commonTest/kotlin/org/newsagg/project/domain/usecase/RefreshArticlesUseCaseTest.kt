package org.newsagg.project.domain.usecase

import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshArticlesUseCaseTest {

    @Test
    fun refreshArticlesUseCase_shouldProxyCallToRepository() = runTest {

        val newsApi = FakeNewsApi()
        val newsDao = FakeNewsDao()
        val fakeRepository = NewsRepositoryImpl(newsApi,newsDao)

        val useCase = RefreshArticlesUseCase(fakeRepository)

        useCase.invoke("headlines")

        assertEquals(expected= 1, actual= newsApi.callCount)
    }
}