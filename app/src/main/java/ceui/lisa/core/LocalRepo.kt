package ceui.lisa.core

abstract class LocalRepo<T> : BaseRepo() {

    abstract fun first(): T?

    abstract fun next(): T?

    override fun hasNext(): Boolean {
        return false
    }

    override fun enableRefresh(): Boolean {
        return true
    }
}
