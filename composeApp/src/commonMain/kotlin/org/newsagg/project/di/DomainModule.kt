package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.domain.usecase.GetNewsUseCase

val domainModule = module {

    factory { GetNewsUseCase(get()) }
}