package org.newsagg.project

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import org.newsagg.project.di.initKoin
import org.newsagg.project.presentation.component.DefaultRootComponent

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin(
            additionalModules = listOf(iosDatabaseModule)
        )
    }
) {
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

    App(rootComponent = root)
}
