package org.newsagg.project.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.newsagg.project.presentation.component.NewsFeedComponent

@Composable
fun NewsFeedScreen(
    component: NewsFeedComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.error != null) {
            Text(
                text = "Error: ${state.error}",
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else if (state.articles.isEmpty()) {
            Text(
                text = "No articles found.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.articles,
                    key = { it.url }
                ) { article ->
                    Text(text = article.title)
                }
            }
        }
    }
}
