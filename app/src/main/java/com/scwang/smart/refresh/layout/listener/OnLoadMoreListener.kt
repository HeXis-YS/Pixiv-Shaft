package com.scwang.smart.refresh.layout.listener

import com.scwang.smart.refresh.layout.api.RefreshLayout

fun interface OnLoadMoreListener {
    fun onLoadMore(refreshLayout: RefreshLayout)
}
