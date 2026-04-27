package org.newsagg.project.di

import org.koin.dsl.module
import org.newsagg.project.data.local.NewsDatabase
import org.newsagg.project.data.local.dao.NewsDao

val databaseModule = module {
    single<NewsDao> {
        get<NewsDatabase>().newsDao()
    }
}