package org.newsagg.project.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveArticlesUseCaseTest {

    @Test
    fun observeUseCase_shouldProxyCallToRepository() = runTest {

        val newsApi = FakeNewsApi()
        val newsDao = FakeNewsDao()
        val fakeRepository = NewsRepositoryImpl(newsApi,newsDao)

        val useCase = ObserveArticlesUseCase(fakeRepository)

        useCase.invoke("headlines").first()

        assertEquals(expected= 1, actual= newsDao.observeCallCount)
    }
}