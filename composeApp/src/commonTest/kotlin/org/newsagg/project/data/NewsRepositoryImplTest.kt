package org.newsagg.project.data

import androidx.paging.testing.asSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.domain.model.Article
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryImplTest {

    private val newsApi = FakeNewsApi()
    private val newsDao = FakeNewsDao()

    private fun createRepository() = NewsRepositoryImpl(newsApi,newsDao)

    @Test
    fun addSubscription_shouldSaveSubscriptionToDao() = runTest {

        val repository = createRepository()
        val topic = "Kotlin"

        repository.addSubscription(topic)

        assertEquals(1, newsDao.subscriptions.value.size)
        assertEquals(topic,newsDao.subscriptions.value.first().topic)

    }

    @Test
    fun getAllSubscriptions_shouldReturnAllSubscriptions() = runTest {

        val repository = createRepository()
        newsDao.addSubscription(SubscriptionDbModel("Kotlin"))
        newsDao.addSubscription(SubscriptionDbModel("Android"))

        val subscriptions = repository.getAllSubscriptions().first()

        assertEquals(subscriptions.size, newsDao.subscriptions.value.size)
        assertEquals(listOf("Kotlin", "Android"), subscriptions)

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

    @Test
    fun observeArticlesPaging_shouldEmitArticlesFromPagingSource() = runTest {
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

        val articles: List<Article> = repository.observeArticlesPaging(topic).asSnapshot()

        assertEquals(1, articles.size)
        assertEquals("Android Studio", articles.first().title)
    }

    @Test
    fun observeArticlesPaging_shouldLoadMoreItemsOnScroll() = runTest {
        val topic = "Android"
        val articlesList = List(50) { index ->
            ArticleDbModel(
                title = "Article $index",
                description = "Description",
                publishedAt = Clock.System.now(),
                sourceName = "Google",
                url = "https://developer.android.com/$index",
                imageUrl = null,
                topic = topic,
                cachedAt = Clock.System.now()
            )
        }
        newsDao.addArticles(articlesList)

        val repository = createRepository()

        val articles: List<Article> = repository.observeArticlesPaging(topic).asSnapshot {
            scrollTo(index = 19)
        }

        assertTrue(articles.size > 20)
    }
}