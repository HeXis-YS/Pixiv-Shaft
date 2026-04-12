package ceui.lisa.core

import android.content.Context
import ceui.lisa.refresh.header.MaterialHeader
import ceui.lisa.refresh.layout.api.RefreshHeader

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
        return MaterialHeader(context)
    }
}
