package org.newsagg.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.newsagg.project.di.initKoin

fun main() = application {
    initKoin(
        additionalModules = listOf(jvmDatabaseModule)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "NewsAggregator",
    ) {
        App()
    }
}
