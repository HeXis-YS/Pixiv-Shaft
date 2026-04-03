package ceui.lisa.cache

fun interface Proxy<T> {
    fun create(): T
}
