package org.newsagg.project

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.dsl.module
import org.newsagg.project.data.local.NewsDatabase
import java.io.File

val jvmDatabaseModule = module {
    single<RoomDatabase.Builder<NewsDatabase>> {
        val userHome = System.getProperty("user.home")
        val appDataDir = File(userHome, ".newsagg")
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        val dbFile = File(appDataDir, "news.db")
        Room.databaseBuilder<NewsDatabase>(
            name = dbFile.absolutePath
        )
    }
}
