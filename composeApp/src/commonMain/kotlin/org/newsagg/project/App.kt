package org.newsagg.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.newsagg.project.presentation.screen.NewsFeedScreen
import org.newsagg.project.presentation.viewmodel.NewsFeedViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        //sdfghjk
        val viewModel: NewsFeedViewModel = koinViewModel()
        NewsFeedScreen(viewModel = viewModel)
    }
}
