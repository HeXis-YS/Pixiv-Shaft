package ceui.lisa.core

import android.content.Context
import ceui.lisa.refresh.layout.api.RefreshFooter
import ceui.lisa.refresh.layout.api.RefreshHeader

interface DataView {

    fun hasNext(): Boolean

    fun enableRefresh(): Boolean

    fun getHeader(context: Context): RefreshHeader

    fun getFooter(context: Context): RefreshFooter

    fun showNoDataHint(): Boolean

    fun token(): String

    fun localData(): Boolean
}
