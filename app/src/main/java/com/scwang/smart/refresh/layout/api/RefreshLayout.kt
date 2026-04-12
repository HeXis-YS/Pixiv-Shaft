package ceui.lisa.refresh.layout.api

import android.content.Context

interface RefreshLayout {
    fun setRefreshHeader(header: RefreshHeader?)
    fun setRefreshFooter(footer: RefreshFooter?)
    fun setEnableRefresh(enable: Boolean)
    fun setEnableLoadMore(enable: Boolean)
    fun setDragRate(rate: Float)
    fun setHeaderTriggerRate(rate: Float)
    fun setHeaderMaxDragRate(rate: Float)
    fun setOnRefreshListener(listener: ceui.lisa.refresh.layout.listener.OnRefreshListener?)
    fun setOnLoadMoreListener(listener: ceui.lisa.refresh.layout.listener.OnLoadMoreListener?)
    fun autoRefresh()
    fun finishRefresh(success: Boolean = true)
    fun finishLoadMore(success: Boolean = true)
    fun setPrimaryColors(vararg colors: Int)

    fun interface DefaultRefreshHeaderCreator {
        fun createRefreshHeader(context: Context, layout: RefreshLayout): RefreshHeader
    }

    fun interface DefaultRefreshFooterCreator {
        fun createRefreshFooter(context: Context, layout: RefreshLayout): RefreshFooter
    }
}
