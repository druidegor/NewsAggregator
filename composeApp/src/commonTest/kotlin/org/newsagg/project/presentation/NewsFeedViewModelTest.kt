package org.newsagg.project.presentation

import app.cash.turbine.test
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
import org.newsagg.project.presentation.viewmodel.NewsFeedState
import org.newsagg.project.presentation.viewmodel.NewsFeedViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NewsFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val newsApi = FakeNewsApi()
    private val newsDao = FakeNewsDao()
    private val repository = NewsRepositoryImpl(newsApi,newsDao)
    private val getTopHeadlinesUseCase = GetTopHeadlinesUseCase(repository)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        headlinesUseCase: GetTopHeadlinesUseCase = getTopHeadlinesUseCase
    ): NewsFeedViewModel {
        return NewsFeedViewModel(
            headlinesUseCase
        )
    }

    @Test
    fun init_block_emits_Loading_then_Success_states() = runTest {


        val viewModel = createViewModel()

        viewModel.state.test {

            assertEquals(NewsFeedState(),awaitItem())

            runCurrent()

            assertEquals(NewsFeedState(emptyList(), isLoading = true, error = null),awaitItem())

            advanceTimeBy(500)

            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals(null,finalState.error)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun init_block_emits_Loading_then_Error_states_when_error_is_thrown() = runTest {

        newsApi.shouldTrowException = Exception("No internet connection")
        val viewModel = createViewModel()

        viewModel.state.test {

            assertEquals(NewsFeedState(),awaitItem())

            runCurrent()

            assertEquals(NewsFeedState(emptyList(), isLoading = true, error = null),awaitItem())

            advanceTimeBy(500)

            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertEquals("No internet connection",finalState.error)
            assertEquals(emptyList(),finalState.articles)

            ensureAllEventsConsumed()
        }
    }
}