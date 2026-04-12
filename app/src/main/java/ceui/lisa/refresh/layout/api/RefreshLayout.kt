package ceui.lisa.refresh.layout.api

interface RefreshLayout {
    fun setEnableRefresh(enable: Boolean)
    fun setEnableLoadMore(enable: Boolean)
    fun setOnRefreshListener(listener: ceui.lisa.refresh.layout.listener.OnRefreshListener?)
    fun setOnLoadMoreListener(listener: ceui.lisa.refresh.layout.listener.OnLoadMoreListener?)
    fun autoRefresh()
    fun finishRefresh(success: Boolean = true)
    fun finishLoadMore(success: Boolean = true)
}
