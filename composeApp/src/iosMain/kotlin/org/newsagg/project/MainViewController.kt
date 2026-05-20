package org.newsagg.project

import androidx.compose.ui.window.ComposeUIViewController
import org.newsagg.project.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin(
            additionalModules = listOf(iosDatabaseModule)
        )
    }
) {
    App()
}
