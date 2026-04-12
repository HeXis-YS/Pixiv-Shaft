package com.scwang.smart.refresh.layout.listener

import com.scwang.smart.refresh.layout.api.RefreshLayout

fun interface OnRefreshListener {
    fun onRefresh(refreshLayout: RefreshLayout)
}
