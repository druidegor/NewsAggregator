package org.newsagg.project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.ArticleDbModel

@Database(
    entities = [ArticleDbModel::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao
}