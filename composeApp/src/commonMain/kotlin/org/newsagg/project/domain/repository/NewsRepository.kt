package org.newsagg.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.newsagg.project.domain.model.Article

interface NewsRepository {

    suspend fun searchNewsByQuery(query: String): List<Article>

    suspend fun getTopHeadlines(): List<Article>
//    suspend fun addNewsToBookmarks(article: Article): Boolean
//
//    suspend fun removeNewsFromBookmarks(article: Article): Boolean
//
//    suspend fun getBookmarks(): List<Article>


}
