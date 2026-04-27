package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase
import org.newsagg.project.domain.usecase.SearchNewsByQueryUseCase


val domainModule = module {

    factory { GetTopHeadlinesUseCase(get()) }
    factory { SearchNewsByQueryUseCase(get()) }
}