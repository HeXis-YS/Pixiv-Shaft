package ceui.lisa.fragments

import com.scwang.smart.refresh.header.FalsifyFooter
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshHeader

interface Swipe {
    fun getSmartRefreshLayout(): SmartRefreshLayout

    fun getHeader(): RefreshHeader

    fun getFooter(): FalsifyFooter

    fun init()
}
