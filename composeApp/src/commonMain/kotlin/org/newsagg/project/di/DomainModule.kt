package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.domain.usecase.ObserveArticlesUseCase
import org.newsagg.project.domain.usecase.RefreshArticlesUseCase


val domainModule = module {

    factory { ObserveArticlesUseCase(get()) }
    factory { RefreshArticlesUseCase(get()) }

}