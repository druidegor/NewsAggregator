package org.newsagg.project.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.domain.usecase.AddSubscriptionUseCase
import org.newsagg.project.domain.usecase.DeleteSubscriptionUseCase
import org.newsagg.project.domain.usecase.GetAllSubscriptionsUseCase
import org.newsagg.project.domain.usecase.ObserveArticlesPagingUseCase
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import org.newsagg.project.presentation.component.DefaultNewsFeedComponent
import org.newsagg.project.presentation.component.NewsFeedComponent

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNewsFeedComponentTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var lifecycle: LifecycleRegistry
    private lateinit var context: DefaultComponentContext
    private val newsApi = FakeNewsApi()
    private val newsDao = FakeNewsDao()
    private val repository = NewsRepositoryImpl(newsApi,newsDao)

    private val observeArticlesPagingUseCase = ObserveArticlesPagingUseCase(repository)
    private val addSubscriptionUseCase= AddSubscriptionUseCase(repository)
    private val deleteSubscriptionUseCase = DeleteSubscriptionUseCase(repository)
    private val getAllSubscriptionsUseCase = GetAllSubscriptionsUseCase(repository)


    @BeforeTest
    fun setUp() {
        lifecycle = LifecycleRegistry()
        context = DefaultComponentContext(lifecycle)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createComponent(
        observeArticles: ObserveArticlesPagingUseCase = observeArticlesPagingUseCase,
    ): NewsFeedComponent {
        return DefaultNewsFeedComponent(
            componentContext = context,
            observeArticlesPagingUseCase = observeArticles,
            addSubscriptionUseCase = addSubscriptionUseCase,
            getAllSubscriptionsUseCase = getAllSubscriptionsUseCase,
            deleteSubscriptionUseCase = deleteSubscriptionUseCase
        )
    }

    @Test
    fun init_shouldLoadSubscriptionsAndSelectFirstTopic() = runTest {
        newsDao.addSubscription(SubscriptionDbModel("Kotlin"))
        newsDao.addSubscription(SubscriptionDbModel("Android"))

        val component = createComponent()
        runCurrent()

        val state = component.state.value
        assertEquals(listOf("Kotlin", "Android"), state.subscriptions)
        assertEquals("Kotlin", state.selectedTopic)
    }

    @Test
    fun onSearchQueryChanged_shouldUpdateQueryInState() = runTest {
        val component = createComponent()

        component.onSearchQueryChanged("KMP")

        assertEquals("KMP", component.state.value.query)
    }

    @Test
    fun onAddSubscription_shouldAddSubscriptionAndResetQuery() = runTest {
        val component = createComponent()
        component.onSearchQueryChanged("   Kotlin   ")

        component.onAddSubscription()
        runCurrent()

        assertEquals("", component.state.value.query)
        assertTrue(newsDao.subscriptions.value.any { it.topic == "Kotlin" })
    }

    @Test
    fun onAddSubscription_whenQueryIsEmpty_shouldDoNothing() = runTest {
        val component = createComponent()
        component.onSearchQueryChanged("   ")

        component.onAddSubscription()
        runCurrent()

        assertTrue(newsDao.subscriptions.value.isEmpty())
    }

    @Test
    fun onDeleteSubscription_shouldRemoveSubscriptionFromDao() = runTest {
        newsDao.addSubscription(SubscriptionDbModel("Kotlin"))
        val component = createComponent()
        runCurrent()

        component.onDeleteSubscription("Kotlin")
        runCurrent()

        assertTrue(newsDao.subscriptions.value.none { it.topic == "Kotlin" })
    }

    @Test
    fun onToggleTopicSelection_shouldSwitchOrDeselectTopic() = runTest {
        newsDao.addSubscription(SubscriptionDbModel("Kotlin"))
        newsDao.addSubscription(SubscriptionDbModel("Android"))
        val component = createComponent()
        runCurrent()

        component.onToggleTopicSelection("Android")
        assertEquals("Android", component.state.value.selectedTopic)

        component.onToggleTopicSelection("Android")
        assertNull(component.state.value.selectedTopic)
    }
}