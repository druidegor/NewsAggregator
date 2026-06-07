package org.newsagg.project.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase
import org.newsagg.project.util.DataResult

interface NewsFeedComponent {

    val state: Value<NewsFeedState>
    fun loadNews()

    data class NewsFeedState(
        val articles: List<Article> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}

class DefaultNewsFeedComponent(
    componentContext: ComponentContext,
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase
): NewsFeedComponent, ComponentContext by componentContext {

    private val _state = MutableValue(NewsFeedComponent.NewsFeedState())
    override val state: Value<NewsFeedComponent.NewsFeedState> = _state

    private val scope = componentContext.coroutineScope()

    init {

    }

    override fun loadNews() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = getTopHeadlinesUseCase()) {
                is DataResult.Success -> {
                    _state.value = _state.value.copy(
                        articles = result.data,
                        isLoading = false,
                        error = null
                    )
                }

                is DataResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Unknown error"
                    )
                }
            }
        }
    }

}

fun ComponentContext.coroutineScope(): CoroutineScope {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    lifecycle.doOnDestroy {
        scope.cancel()
    }
    return scope
}