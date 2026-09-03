package org.newsagg.project.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.NewsRemoteKeys
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
    fun pagingSource(topic: String): PagingSource<Int, ArticleDbModel>

    @Query("DELETE FROM articles WHERE topic = :topic")
    suspend fun clearArticlesByTopic(topic: String)
    @Insert(onConflict = REPLACE)
    suspend fun addArticles(articles: List<ArticleDbModel>)

    @Query("SELECT COUNT(*) FROM articles WHERE topic = :topic")
    suspend fun getArticlesCountByTopic(topic: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: NewsRemoteKeys)

    @Query("SELECT * FROM news_remote_keys WHERE topic = :topic")
    suspend fun getRemoteKeyByTopic(topic: String): NewsRemoteKeys?

    @Query("DELETE FROM news_remote_keys WHERE topic = :topic")
    suspend fun deleteKeyByTopic(topic: String)

    @Transaction
    suspend fun saveArticlesAndKeysTransaction(
        topic: String,
        articles: List<ArticleDbModel>,
        remoteKey: NewsRemoteKeys,
        shouldClear: Boolean
    ) {
        if (shouldClear) {
            clearArticlesByTopic(topic)
            deleteKeyByTopic(topic)
        }
        insertKey(remoteKey)
        addArticles(articles)
    }

}