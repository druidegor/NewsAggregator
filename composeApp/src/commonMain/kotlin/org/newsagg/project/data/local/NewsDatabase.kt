
package org.newsagg.project.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Transaction
import androidx.room.TypeConverters
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.NewsRemoteKeys
import org.newsagg.project.data.local.model.SubscriptionDbModel

@Database(
    entities = [ArticleDbModel::class, SubscriptionDbModel::class, NewsRemoteKeys::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
@ConstructedBy(NewsDatabaseConstructor::class)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NewsDatabaseConstructor : RoomDatabaseConstructor<NewsDatabase> {
    override fun initialize(): NewsDatabase
}