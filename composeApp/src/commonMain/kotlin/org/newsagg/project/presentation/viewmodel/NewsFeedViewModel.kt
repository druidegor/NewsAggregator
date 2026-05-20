package org.newsagg.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase
import org.newsagg.project.util.DataResult

data class NewsFeedState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NewsFeedViewModel(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewsFeedState())
    val state: StateFlow<NewsFeedState> = _state.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getTopHeadlinesUseCase()) {
                is DataResult.Success -> {
                    _state.update {
                        it.copy(
                            articles = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is DataResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Unknown error"
                        )
                    }
                }
            }
        }
    }
}
