package org.newsagg.project.domain.usecase

import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTopHeadlinesUseCaseTest {

    @Test
    fun getTopHeadlinesUseCase_shouldProxyCallToRepository() = runTest {

        val newsApi = FakeNewsApi()
        val newsDao = FakeNewsDao()
        val fakeRepository = NewsRepositoryImpl(newsApi,newsDao)

        val useCase = GetTopHeadlinesUseCase(fakeRepository)

        useCase.invoke()

        assertEquals(expected= 1, actual= newsApi.callCount)
    }
}