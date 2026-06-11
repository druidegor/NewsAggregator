package org.newsagg.project

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import org.newsagg.project.di.initKoin
import org.newsagg.project.presentation.component.DefaultRootComponent
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin(additionalModules = listOf(iosDatabaseModule))

    val koin = object : KoinComponent {}.getKoin()

    val root = koin.get<DefaultRootComponent> {
        parametersOf(DefaultComponentContext(lifecycle = ApplicationLifecycle()))
    }
    return ComposeUIViewController { App(rootComponent = root) }
}
