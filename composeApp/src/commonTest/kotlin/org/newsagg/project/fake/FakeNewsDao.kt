package org.newsagg.project.fake

import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.NewsRemoteKeys
import org.newsagg.project.data.local.model.SubscriptionDbModel
import org.newsagg.project.domain.model.Article
import kotlin.time.Clock

class FakeNewsDao: NewsDao {

    var observeCallCount = 0
    val subscriptions = MutableStateFlow<List<SubscriptionDbModel>>(emptyList())
    val articles = MutableStateFlow<List<ArticleDbModel>>(emptyList())
    val remoteKeys = MutableStateFlow<List<NewsRemoteKeys>>(emptyList())

    override suspend fun addArticles(articles: List<ArticleDbModel>) {
        this.articles.value += articles
    }

    override suspend fun addSubscription(subscription: SubscriptionDbModel) {
        subscriptions.value += subscription
    }

    override suspend fun deleteSubscription(subscription: SubscriptionDbModel) {
        subscriptions.value = subscriptions.value.filter { it != subscription }
        articles.value = articles.value.filter { it.topic != subscription.topic}
    }

    override fun getAllSubscriptions(): Flow<List<SubscriptionDbModel>> {
        return subscriptions
    }

    override suspend fun clearArticlesByTopic(topic: String) {
        articles.value = articles.value.filterNot { it.topic == topic }
    }

    override suspend fun deleteKeyByTopic(topic: String) {
        remoteKeys.value = remoteKeys.value.filter { it.topic != topic }
    }

    override suspend fun getArticlesCountByTopic(topic: String): Int {
        return articles.value.count { it.topic == topic }
    }

    override suspend fun getRemoteKeyByTopic(topic: String): NewsRemoteKeys? {
        return remoteKeys.value.find { it.topic == topic }
    }

    override suspend fun insertKey(key: NewsRemoteKeys) {
        remoteKeys.update { it + key }
    }

    override fun pagingSource(topic: String): PagingSource<Int, ArticleDbModel> {
        observeCallCount++

        return FakeArticlePagingSource {
            articles.value.filter { it.topic == topic }
        }
    }

}

