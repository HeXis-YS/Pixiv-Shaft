package ceui.lisa.fragments

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import ceui.lisa.refresh.layout.SmartRefreshLayout

abstract class SwipeFragment<T : ViewBinding> : BaseLazyFragment<T>(), Swipe {
    override fun init() {
        val layout: SmartRefreshLayout? = getSmartRefreshLayout()
        if (layout != null) {
            layout.setEnableRefresh(true)
            layout.setEnableLoadMore(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    open fun enableRefresh(): Boolean = true

    open fun enableLoadMore(): Boolean = true
}
