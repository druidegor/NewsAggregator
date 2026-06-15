package org.newsagg.project.di

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import org.newsagg.project.presentation.component.DefaultRootComponent

val screenModule = module {

    factory { (context: ComponentContext) ->
        DefaultRootComponent(
            context,
            observeArticlesUseCase = get(),
            refreshArticlesUseCase = get()
        )
    }
}