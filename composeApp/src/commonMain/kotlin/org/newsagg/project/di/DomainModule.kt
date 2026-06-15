package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase


val domainModule = module {

    factory { GetTopHeadlinesUseCase(get()) }
}