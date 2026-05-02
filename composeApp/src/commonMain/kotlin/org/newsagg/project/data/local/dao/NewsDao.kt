package org.newsagg.project.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
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

    @Query("SELECT * FROM articles WHERE topic in (:topics) ORDER BY publishedAt DESC")
    fun getArticlesByTopic(topics: List<String>): Flow<List<ArticleDbModel>>

    @Insert(onConflict = IGNORE)
    suspend fun addArticles(articles: List<ArticleDbModel>)

    @Query("DELETE FROM articles WHERE topic IN (:topics)")
    suspend fun deleteArticlesByTopics(topics: List<String>)
}