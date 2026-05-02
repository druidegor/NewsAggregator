package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.domain.usecase.AddSubscriptionUseCase
import org.newsagg.project.domain.usecase.DeleteSubscriptionUseCase
import org.newsagg.project.domain.usecase.GetAllSubscriptionsUseCase
import org.newsagg.project.domain.usecase.GetArticlesByTopicsUseCase
import org.newsagg.project.domain.usecase.GetTopHeadlinesUseCase


val domainModule = module {

    factory { GetTopHeadlinesUseCase(get()) }
    factory { GetArticlesByTopicsUseCase(get()) }
    factory { GetAllSubscriptionsUseCase(get()) }
    factory { DeleteSubscriptionUseCase(get()) }
    factory { AddSubscriptionUseCase(get()) }
}