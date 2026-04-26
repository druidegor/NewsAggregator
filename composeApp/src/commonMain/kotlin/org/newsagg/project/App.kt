package org.newsagg.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

import newsaggregator.composeapp.generated.resources.Res
import newsaggregator.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.koinInject
import org.newsagg.project.data.network.api.NewsApi
import org.newsagg.project.presentation.screen.NewsFeedScreen

@Composable
@Preview
fun App() {

    MaterialTheme {
        NewsFeedScreen()
    }
}