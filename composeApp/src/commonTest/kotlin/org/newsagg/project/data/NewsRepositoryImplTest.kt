package org.newsagg.project.data

import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import org.newsagg.project.util.DataResult
import kotlin.test.Test
import kotlin.test.assertIs

class NewsRepositoryImplTest {

    @Test
    fun loadArticles_whenNetworkFails_returnDataResultError()  = runTest {

        val newsApi = FakeNewsApi()
        val newsDao = FakeNewsDao()

        newsApi.shouldTrowException = Exception("No internet connection")

        val repository = NewsRepositoryImpl(newsApi, newsDao)

        val result = repository.loadArticles("kotlin")

        assertIs<DataResult.Error>(result)
    }
}