package org.newsagg.project.fake

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.newsagg.project.data.local.model.ArticleDbModel

class FakeArticlePagingSource(
    private val getItems: () -> List<ArticleDbModel>
) : PagingSource<Int, ArticleDbModel>() {

    override fun getRefreshKey(state: PagingState<Int, ArticleDbModel>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDbModel> {
        return LoadResult.Page(
            data = getItems(),
            prevKey = null,
            nextKey = null
        )
    }
}