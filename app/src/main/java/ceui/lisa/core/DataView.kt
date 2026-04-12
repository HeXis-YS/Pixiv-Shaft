package ceui.lisa.core

interface DataView {

    fun hasNext(): Boolean

    fun enableRefresh(): Boolean

    fun showNoDataHint(): Boolean

    fun token(): String

    fun localData(): Boolean
}
