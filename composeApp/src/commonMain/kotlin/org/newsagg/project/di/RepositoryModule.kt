package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.data.repository.NewsRepositoryImpl
import org.newsagg.project.domain.repository.NewsRepository

val repositoryModule = module {

    single<NewsRepository> { NewsRepositoryImpl(get()) }
}