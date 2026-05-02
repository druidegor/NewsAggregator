package org.newsagg.project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel

@Database(
    entities = [ArticleDbModel::class, SubscriptionDbModel::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao
}