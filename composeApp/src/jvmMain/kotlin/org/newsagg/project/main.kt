package org.newsagg.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import org.newsagg.project.di.initKoin
import org.newsagg.project.presentation.component.DefaultRootComponent

fun main() {

    initKoin(additionalModules = listOf(jvmDatabaseModule))

    val koin = object: KoinComponent {}.getKoin()

    val lifecycle = LifecycleRegistry()
    val root = koin.get<DefaultRootComponent> { parametersOf(DefaultComponentContext(lifecycle)) }

    application {

        val windowState = rememberWindowState()

        LifecycleController(lifecycle,windowState)

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "NewsAggregator",
        ) {
            App(rootComponent = root)
        }
    }
}
