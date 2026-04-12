package ceui.lisa.fragments

import ceui.lisa.refresh.layout.SmartRefreshLayout

interface Swipe {
    fun getSmartRefreshLayout(): SmartRefreshLayout

    fun init()
}
