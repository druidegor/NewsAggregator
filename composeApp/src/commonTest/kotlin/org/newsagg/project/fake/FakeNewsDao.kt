package org.newsagg.project.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.newsagg.project.data.local.dao.NewsDao
import org.newsagg.project.data.local.model.ArticleDbModel
import org.newsagg.project.data.local.model.SubscriptionDbModel

class FakeNewsDao: NewsDao {

    var observeCallCount = 0
    val subscriptions = MutableStateFlow<List<SubscriptionDbModel>>(emptyList())
    val articles = MutableStateFlow<List<ArticleDbModel>>(emptyList())

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

    override fun observeArticles(topic: String): Flow<List<ArticleDbModel>> {
        observeCallCount++
        return articles.map { list -> list.filter { it.topic == topic } }
    }


}