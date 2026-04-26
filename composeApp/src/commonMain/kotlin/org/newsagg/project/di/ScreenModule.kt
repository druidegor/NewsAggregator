package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.presentation.viewmodel.NewsFeedViewModel

val screenModule = module {

    factory { NewsFeedViewModel(get()) }
}