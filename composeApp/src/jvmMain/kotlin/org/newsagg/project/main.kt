package org.newsagg.project

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import org.newsagg.project.di.initKoin
import org.newsagg.project.presentation.component.DefaultRootComponent

fun main() = application {
    initKoin(
        additionalModules = listOf(jvmDatabaseModule)
    )
    val lifecycle = remember { LifecycleRegistry() }
    val root = remember {
        DefaultRootComponent(componentContext = DefaultComponentContext(lifecycle = lifecycle))
    }

    DisposableEffect(Unit) {
        lifecycle.resume()
        onDispose {
            lifecycle.destroy()
        }
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "NewsAggregator",
    ) {
        App(rootComponent = root)
    }
}
