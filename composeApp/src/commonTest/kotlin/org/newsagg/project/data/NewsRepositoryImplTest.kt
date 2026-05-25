package org.newsagg.project.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import org.newsagg.project.util.DataResult
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryImplTest {

    private val newsApi = FakeNewsApi()
    private val newsDao = FakeNewsDao()

    private fun createRepository() = NewsRepositoryImpl(newsApi,newsDao)

    @Test
    fun loadArticles_whenNetworkFails_returnDataResultError()  = runTest {

        newsApi.shouldTrowException = Exception("No internet connection")

        val repository = createRepository()

        val result = repository.loadArticles("kotlin")

        assertIs<DataResult.Error>(result)
    }

    @Test
    fun loadArticles_whenCoroutineStopped_cancellationExceptionTrows() = runTest {

        val repository = createRepository()

        val deferredResult = backgroundScope.async{
           repository.loadArticles("kotlin")
        }

        runCurrent()

        deferredResult.cancel()

        assertFailsWith<CancellationException> {
            deferredResult.await()
        }
    }

    @Test
    fun getTopHeadlines_whenNetworkFails_returnDataResultError() = runTest {

        newsApi.shouldTrowException = Exception("No internet connection")

        val repository = createRepository()

        val result = repository.getTopHeadlines()

        assertIs<DataResult.Error>(result)
    }

    @Test
    fun getTopHeadlines_whenCoroutineStopped_cancellationExceptionCalled() = runTest {

        val repository = createRepository()

        val deferredResult = backgroundScope.async{
            repository.loadArticles("kotlin")
        }

        runCurrent()

        deferredResult.cancel()

        assertFailsWith<CancellationException> {
            deferredResult.await()
        }
    }
}