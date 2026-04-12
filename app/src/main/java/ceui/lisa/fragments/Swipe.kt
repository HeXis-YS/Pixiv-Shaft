package ceui.lisa.fragments

import ceui.lisa.refresh.header.FalsifyFooter
import ceui.lisa.refresh.layout.SmartRefreshLayout
import ceui.lisa.refresh.layout.api.RefreshHeader

interface Swipe {
    fun getSmartRefreshLayout(): SmartRefreshLayout

    fun getHeader(): RefreshHeader

    fun getFooter(): FalsifyFooter

    fun init()
}
