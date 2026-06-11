package org.newsagg.project.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase
import org.newsagg.project.fake.FakeNewsApi
import org.newsagg.project.fake.FakeNewsDao
import org.newsagg.project.presentation.component.DefaultNewsFeedComponent
import org.newsagg.project.presentation.component.NewsFeedComponent

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNewsFeedComponentTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var lifecycle: LifecycleRegistry
    private lateinit var context: DefaultComponentContext
    private val newsApi = FakeNewsApi()
    private val newsDao = FakeNewsDao()
    private val repository = NewsRepositoryImpl(newsApi,newsDao)
    private val getTopHeadlinesUseCase = GetTopHeadlinesUseCase(repository)

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
        headlinesUseCase: GetTopHeadlinesUseCase = getTopHeadlinesUseCase
    ): NewsFeedComponent {
        return DefaultNewsFeedComponent(
            componentContext = context,
            getTopHeadlinesUseCase = headlinesUseCase
        )
    }

    @Test
    fun init_block_emits_Loading_then_Success_states() = runTest {


        val component = createComponent()

        assertEquals(NewsFeedComponent.NewsFeedState(),component.state.value)

        lifecycle.resume()
        runCurrent()

        assertEquals(NewsFeedComponent.NewsFeedState(emptyList(), isLoading = true, error = null),component.state.value)

        advanceTimeBy(500)
        runCurrent()

        val finalState = component.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(null,finalState.error)

    }

    @Test
    fun init_block_emits_Loading_then_Error_states_when_error_is_thrown() = runTest {

        newsApi.shouldTrowException = Exception("No internet connection")
        val component = createComponent()

        assertEquals(NewsFeedComponent.NewsFeedState(),component.state.value)

        runCurrent()

        assertEquals(NewsFeedComponent.NewsFeedState(emptyList(), isLoading = true, error = null),component.state.value)

        advanceTimeBy(500)
        runCurrent()

        val finalState = component.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals("No internet connection",finalState.error)
        assertEquals(emptyList(),finalState.articles)

    }
}