package org.newsagg.project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase

class NewsFeedViewModel(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase
): ViewModel() {

    private val _state = MutableStateFlow(NewsFeedState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = NewsFeedState(isLoading = true)
            try {
                println("VIEWMODEL: ")
                val articles = getTopHeadlinesUseCase()
                println("VIEWMODEL: $articles")
                _state.value = NewsFeedState(articles = articles, isLoading = false)
            } catch (e: Exception) {
            }
        }
    }
}

data class NewsFeedState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
