package ceui.lisa.fragments

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import ceui.lisa.refresh.header.FalsifyFooter
import ceui.lisa.refresh.header.FalsifyHeader
import ceui.lisa.refresh.layout.SmartRefreshLayout
import ceui.lisa.refresh.layout.api.RefreshHeader

abstract class SwipeFragment<T : ViewBinding> : BaseLazyFragment<T>(), Swipe {
    override fun getHeader(): RefreshHeader = FalsifyHeader(mContext)

    override fun getFooter(): FalsifyFooter = FalsifyFooter(mContext)

    override fun init() {
        val layout: SmartRefreshLayout? = getSmartRefreshLayout()
        if (layout != null) {
            layout.setEnableRefresh(true)
            layout.setEnableLoadMore(true)
            layout.setRefreshHeader(getHeader())
            layout.setRefreshFooter(getFooter())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    open fun enableRefresh(): Boolean = true

    open fun enableLoadMore(): Boolean = true
}
