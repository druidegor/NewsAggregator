package org.newsagg.project

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.newsagg.project.data.local.NewsDatabase

val androidDatabaseModule = module {
    single<NewsDatabase>
        {
            val context = androidContext()
            val dbFile = context.getDatabasePath("news.db")

            Room.databaseBuilder<NewsDatabase>(
                context,
                dbFile.absolutePath
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
}