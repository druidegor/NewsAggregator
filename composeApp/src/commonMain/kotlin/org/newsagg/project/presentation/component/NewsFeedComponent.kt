package org.newsagg.project.presentation.component

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.newsagg.project.domain.model.Article
import org.newsagg.project.domain.usecase.AddSubscriptionUseCase
import org.newsagg.project.domain.usecase.DeleteSubscriptionUseCase
import org.newsagg.project.domain.usecase.GetAllSubscriptionsUseCase
import org.newsagg.project.domain.usecase.ObserveArticlesPagingUseCase
import org.newsagg.project.util.coroutineScope
import kotlin.collections.emptyList

interface NewsFeedComponent {

    val pagingArticles: Flow<PagingData<Article>>
    val state: Value<NewsFeedState>

    fun onSearchQueryChanged(query: String)
    fun onAddSubscription()
    fun onDeleteSubscription(topic: String)
    fun onToggleTopicSelection(topic: String)

    data class NewsFeedState(
        val query: String = "",
        val subscriptions: List<String> = emptyList(),
        val selectedTopic: String? = null
    )
}

class DefaultNewsFeedComponent(
    componentContext: ComponentContext,
    private val getAllSubscriptionsUseCase: GetAllSubscriptionsUseCase,
    private val observeArticlesPagingUseCase: ObserveArticlesPagingUseCase,
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase
): NewsFeedComponent, ComponentContext by componentContext {

    private val _state = MutableValue(NewsFeedComponent.NewsFeedState())
    override val state: Value<NewsFeedComponent.NewsFeedState> = _state

    private val scope = componentContext.coroutineScope()

    private val selectedTopicFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val pagingArticles: Flow<PagingData<Article>> = selectedTopicFlow
        .flatMapLatest { topic ->
            if (!topic.isNullOrEmpty()) {
                observeArticlesPagingUseCase(topic)
            } else {
                flowOf(PagingData.empty())
            }
        }.cachedIn(scope)

    init {
        scope.launch {
            getAllSubscriptionsUseCase().collect { subscriptions ->
                val currentSelected = _state.value.selectedTopic

                val newSelected = if (subscriptions.contains(currentSelected)) {
                    currentSelected
                } else {
                    subscriptions.firstOrNull()
                }

                _state.value = _state.value.copy(
                    subscriptions = subscriptions,
                    selectedTopic = newSelected
                )
                selectedTopicFlow.value = newSelected

            }
        }
    }

    override fun onAddSubscription() {
        val currentQuery = _state.value.query.trim()
        if (currentQuery.isEmpty()) return

        scope.launch {
            addSubscriptionUseCase(currentQuery)
            _state.value = _state.value.copy(query = "")
        }
    }

    override fun onDeleteSubscription(topic: String) {
        scope.launch {
            deleteSubscriptionUseCase(topic)
        }
    }

    override fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    override fun onToggleTopicSelection(topic: String) {
        val currentSelected = _state.value.selectedTopic
        val newSelected = if (currentSelected == topic) null else topic

        _state.value = _state.value.copy(selectedTopic = newSelected)
        selectedTopicFlow.value = newSelected
    }

}