package org.newsagg.project.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.newsagg.project.presentation.viewmodel.NewsFeedViewModel

@Composable
fun NewsFeedScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsFeedViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()

    if (!state.isLoading) {
        Box(modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = state.articles.first().title)
        }
    }

}