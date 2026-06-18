package org.newsagg.project.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.network.model.ArticleDto
import org.newsagg.project.data.network.model.NewsResponseDto
import org.newsagg.project.data.network.model.SourceDto
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.domain.model.Article
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import org.newsagg.project.util.DataResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryImplTest {

    private val newsApi = FakeNewsApi()
    private val newsDao = FakeNewsDao()

    private fun createRepository() = NewsRepositoryImpl(newsApi,newsDao)

    @Test
    fun observeArticles_returnNotEmptyListOfArticles() = runTest {

        val topic = "Android"

        val androidArticle = ArticleDbModel(
            title = "Android Studio",
            description = "New features available",
            publishedAt = Clock.System.now(),
            sourceName = "Google",
            url = "https://developer.android.com",
            imageUrl = null,
            topic = topic,
            cachedAt = Clock.System.now()
        )

        newsDao.addArticles(listOf(androidArticle))

        val repository = createRepository()

        val result = repository.observeArticles(topic).first()

        assertEquals(1,result.size)
        assertIs<Article>(result.first())
    }

    @Test
    fun refreshArticles_whenNetworkFails_returnDataResultError()  = runTest {

        newsApi.shouldTrowException = Exception("No internet connection")

        val repository = createRepository()

        val result = repository.refreshArticles("kotlin")

        assertIs<DataResult.Error>(result)
    }

    @Test
    fun refreshArticles_whenCoroutineStopped_cancellationExceptionIsThrown() = runTest {

        val repository = createRepository()

        val deferredResult = backgroundScope.async{
           repository.refreshArticles("kotlin")
        }

        runCurrent()

        deferredResult.cancel()

        assertFailsWith<CancellationException> {
            deferredResult.await()
        }
    }

    @Test
    fun refreshArticles_shouldFetchFromApiAndSaveToDaoOnSuccess() = runTest{

        val dto = NewsResponseDto(
            articles = listOf(
                ArticleDto(
                    source = SourceDto(
                        name = "TechCrunch"
                    ),
                    title = "Kotlin Multiplatform",
                    description = "Exploring the latest trends",
                    url = "https://techcrunch.com/2026/06/17/kmp-state-2026",
                    urlToImage = "https://techcrunch.com/images/kmp-banner.jpg",
                    publishedAt = "2026-06-17T09:00:00Z"
                ),
                ArticleDto(
                    source = SourceDto(
                        name = "Android Weekly"
                    ),
                    title = "Deep dive into Room Database testing",
                    description = "A comprehensive guide on using in-memory databases ",
                    url = "https://androidweekly.net/articles/room-testing-guide",
                    urlToImage = null,
                    publishedAt = "2026-06-16T14:30:00Z"
                )
            )
        )
        newsApi.newsProvideResponse = { dto }
        val repository = createRepository()

        val result = repository.refreshArticles("Kotlin")

        assertIs<DataResult.Success<Unit>>(result)
        assertEquals(2,newsDao.articles.value.size)
        assertEquals("Kotlin Multiplatform",newsDao.articles.value.first().title)
    }

    @Test
    fun refreshArticles_shouldAddSubscriptionToDao() = runTest {

        val repository = createRepository()

        repository.refreshArticles("Kotlin")

        assertEquals(1,newsDao.subscriptions.value.size)
        assertEquals("Kotlin", newsDao.subscriptions.value.first().topic)
    }

    @Test
    fun deleteSubscription_shouldRemoveSubscriptionAndCascadeDeleteArticles() = runTest {

        val topicToDelete = "kotlin"
        val otherTopic = "android"

        newsDao.addSubscription(SubscriptionDbModel(topic = topicToDelete))

        val now = Clock.System.now()
        val kotlinArticle = ArticleDbModel(
            title = "Kotlin Multiplatform",
            description = "Shared code is awesome",
            publishedAt = now,
            sourceName = "JetBrains",
            url = "https://kotlinlang.org/kmp",
            imageUrl = null,
            topic = topicToDelete,
            cachedAt = now
        )
        val androidArticle = ArticleDbModel(
            title = "Android Studio",
            description = "New features available",
            publishedAt = now,
            sourceName = "Google",
            url = "https://developer.android.com",
            imageUrl = null,
            topic = otherTopic,
            cachedAt = now
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