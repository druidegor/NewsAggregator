package org.newsagg.project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.newsagg.project.data.local.model.ArticleDbModel

@Dao
interface NewsDao {

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getNews(): Flow<List<ArticleDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNews(articles: List<ArticleDbModel>)
}