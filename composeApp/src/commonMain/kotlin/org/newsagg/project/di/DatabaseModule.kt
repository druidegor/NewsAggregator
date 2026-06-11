package org.newsagg.project.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import org.newsagg.project.data.local.NewsDatabase
import org.newsagg.project.data.local.dao.NewsDao

val databaseModule = module {
    single {
        val builder = get<RoomDatabase.Builder<NewsDatabase>>()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<NewsDao> { get<NewsDatabase>().newsDao() }
}
