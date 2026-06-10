package org.newsagg.project.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import org.newsagg.project.util.DataResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
    fun loadArticles_whenCoroutineStopped_cancellationExceptionIsThrown() = runTest {

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

    @Test
    fun deleteSubscription_shouldRemoveSubscriptionAndCascadeDeleteArticles() = runTest {

        val topicToDelete = "kotlin"
        val otherTopic = "android"

        newsDao.addSubscription(SubscriptionDbModel(topic = topicToDelete))

        val kotlinArticle = ArticleDbModel(
            title = "Kotlin Multiplatform",
            description = "Shared code is awesome",
            publishedAt = 1640995200000L,
            sourceName = "JetBrains",
            url = "https://kotlinlang.org/kmp",
            imageUrl = null,
            topic = topicToDelete
        )
        val androidArticle = ArticleDbModel(
            title = "Android Studio",
            description = "New features available",
            publishedAt = 1640995200000L,
            sourceName = "Google",
            url = "https://developer.android.com",
            imageUrl = null,
            topic = otherTopic
        )
        newsDao.addArticles(listOf(kotlinArticle, androidArticle))

        val repository = createRepository()

        repository.deleteSubscription(topicToDelete)

        assertTrue(newsDao.subscriptions.value.isEmpty())

        val remainingArticles = newsDao.articles.value
        assertEquals(1, remainingArticles.size)
        assertEquals(otherTopic, remainingArticles.first().topic)
    }

    @Test
    fun deleteSubscription_whenNoSubscriptionsExist_shouldNotCrashAndRemainEmpty() = runTest {
        val repository = createRepository()

        repository.deleteSubscription("kotlin")

        assertTrue(newsDao.subscriptions.value.isEmpty())
        assertTrue(newsDao.articles.value.isEmpty())
    }
}