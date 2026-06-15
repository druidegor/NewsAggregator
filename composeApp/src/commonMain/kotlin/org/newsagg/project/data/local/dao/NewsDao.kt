package org.newsagg.project.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel

@Dao
interface NewsDao {

    @Query("SELECT *FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionDbModel>>

    @Insert(onConflict = IGNORE)
    suspend fun addSubscription(subscription: SubscriptionDbModel)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionDbModel)

    @Query("SELECT * FROM articles WHERE topic in (:topic) ORDER BY publishedAt DESC")
    fun observeArticles(topic: String): Flow<List<ArticleDbModel>>

    @Insert(onConflict = REPLACE)
    suspend fun addArticles(articles: List<ArticleDbModel>)

}