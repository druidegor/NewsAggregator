package org.newsagg.project

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.dsl.module
import org.newsagg.project.data.local.NewsDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

val iosDatabaseModule = module {
    single<RoomDatabase.Builder<NewsDatabase>> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val path = requireNotNull(documentDirectory?.path) + "/news.db"
        Room.databaseBuilder<NewsDatabase>(
            name = path
        )
    }
}
