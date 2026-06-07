package org.newsagg.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.koin.compose.viewmodel.koinViewModel
import org.newsagg.project.presentation.component.RootComponent
import org.newsagg.project.presentation.screen.NewsFeedScreen

@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {

        Children(
            stack = rootComponent.childStack,
            animation = stackAnimation(slide())
        ) { target ->
            when (val child = target.instance) {
                is RootComponent.Child.Feed -> NewsFeedScreen(component = child.component)
            }

        }
    }
}
