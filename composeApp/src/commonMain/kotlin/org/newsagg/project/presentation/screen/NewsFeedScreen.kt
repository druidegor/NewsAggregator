package org.newsagg.project.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.newsagg.project.domain.model.Article
import org.newsagg.project.presentation.component.NewsFeedComponent

@Composable
fun NewsFeedScreen(
    component: NewsFeedComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val lazyPagingItems = component.pagingArticles.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 16.dp)
        ) {
            SubscriptionInputField(
                query = state.query,
                onQueryChanged = component::onSearchQueryChanged,
                onAddClick = component::onAddSubscription
            )

            Spacer(modifier = Modifier.height(16.dp))

            SubscriptionChipsRow(
                subscriptions = state.subscriptions,
                selectedTopic = state.selectedTopic,
                onToggleSelection = component::onToggleTopicSelection,
                onDeleteSubscription = component::onDeleteSubscription
            )

            Spacer(modifier = Modifier.height(8.dp))

            NewsList(
                lazyPagingItems = lazyPagingItems,
                hasSelectedTopic = state.selectedTopic != null
            )
        }
    }
}

@Composable
private fun SubscriptionInputField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Поиск и добавление новых тем") },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить тему в подписки",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { if (query.isNotBlank()) onAddClick() }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionChipsRow(
    subscriptions: List<String>,
    selectedTopic: String?,
    onToggleSelection: (String) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (subscriptions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Нет активных подписок. Добавьте тему выше.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val sortedSubscriptions = remember(subscriptions, selectedTopic) {
            if (selectedTopic != null && subscriptions.contains(selectedTopic)) {
                listOf(selectedTopic) + (subscriptions - selectedTopic)
            } else {
                subscriptions
            }
        }

        LazyRow(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(sortedSubscriptions, key = { it }) { topic ->
                val isSelected = topic == selectedTopic

                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleSelection(topic) },
                    label = { Text(text = topic) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Выбрано",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Удалить тему",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onDeleteSubscription(topic) }
                        )
                    }
                )
            }
        }
    }
}
@Composable
private fun NewsList(
    lazyPagingItems: LazyPagingItems<Article>,
    hasSelectedTopic: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!hasSelectedTopic) {
            Text(
                text = "Выберите тему, чтобы прочитать новости.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Box
        }

        val refreshState = lazyPagingItems.loadState.refresh

        if (lazyPagingItems.itemCount == 0 && refreshState is LoadState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (lazyPagingItems.itemCount == 0 && refreshState is LoadState.Error) {
            ErrorMessage(
                message = "Не удалось загрузить новости.",
                onRetry = { lazyPagingItems.retry() },
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (lazyPagingItems.itemCount == 0 && refreshState is LoadState.NotLoading) {
            Text(
                text = "В этой категории пока нет новостей.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = { index ->
                        lazyPagingItems.peek(index)?.url ?: index
                    }
                ) { index ->
                    val article = lazyPagingItems[index]
                    if (article != null) {
                        ArticleItem(article = article)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleItem(
    article: Article,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (!article.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        ) {
            Text("Повторить")
        }
    }
}