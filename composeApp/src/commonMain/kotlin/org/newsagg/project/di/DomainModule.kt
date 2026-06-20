package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.domain.usecase.AddSubscriptionUseCase
import org.newsagg.project.domain.usecase.DeleteSubscriptionUseCase
import org.newsagg.project.domain.usecase.GetAllSubscriptionsUseCase
import org.newsagg.project.domain.usecase.ObserveArticlesPagingUseCase


val domainModule = module {

    factory { ObserveArticlesPagingUseCase(get()) }
    factory { GetAllSubscriptionsUseCase(get()) }
    factory { AddSubscriptionUseCase(get()) }
    factory { DeleteSubscriptionUseCase(get()) }

}