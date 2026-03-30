package ceui.lisa.core

import android.content.Context
import com.scwang.smart.refresh.layout.api.RefreshHeader
import ceui.lisa.view.MyDeliveryHeader

abstract class LocalRepo<T> : BaseRepo() {

    abstract fun first(): T?

    abstract fun next(): T?

    override fun hasNext(): Boolean {
        return false
    }

    override fun enableRefresh(): Boolean {
        return true
    }

    override fun getHeader(context: Context): RefreshHeader {
        return MyDeliveryHeader(context)
    }
}
