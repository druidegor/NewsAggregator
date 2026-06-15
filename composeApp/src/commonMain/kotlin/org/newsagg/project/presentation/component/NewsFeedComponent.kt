package org.newsagg.project.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.usecase.ObserveArticlesUseCase
import org.newsagg.project.domain.usecase.RefreshArticlesUseCase
import org.newsagg.project.util.DataResult
import org.newsagg.project.util.coroutineScope
import kotlin.collections.emptyList

interface NewsFeedComponent {

    val state: Value<NewsFeedState>
    fun loadNews()

    data class NewsFeedState(
        val articles: List<Article> = emptyList(),
        val refreshing: Boolean = false,
        val error: String? = null
    )
}

class DefaultNewsFeedComponent(
    componentContext: ComponentContext,
    private val observeArticlesUseCase: ObserveArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase
): NewsFeedComponent, ComponentContext by componentContext {

    private val _state = MutableValue(NewsFeedComponent.NewsFeedState())
    override val state: Value<NewsFeedComponent.NewsFeedState> = _state

    private val scope = componentContext.coroutineScope()

    private val articlesStateFlow = observeArticlesUseCase("headlines").stateIn(
        scope = scope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    init {
        scope.launch {
            articlesStateFlow.collect { articles ->
                _state.value = _state.value.copy(articles = articles)
            }
        }

        loadNews()
    }

    override fun loadNews() {
        scope.launch {
            _state.value = _state.value.copy(refreshing = true, error = null)
            when (val result = refreshArticlesUseCase("headlines")) {
                is DataResult.Success -> {
                    _state.value = _state.value.copy(
                        refreshing = false,
                        error = null
                    )
                }
                is DataResult.Error -> {
                    _state.value = _state.value.copy(
                        refreshing = false,
                        error = result.throwable.message ?: "Unknown error"
                    )
                }
            }
        }
    }

}