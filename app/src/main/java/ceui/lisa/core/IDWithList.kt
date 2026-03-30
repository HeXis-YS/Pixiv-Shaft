package ceui.lisa.core

interface IDWithList<T> {
    fun getUUID(): String

    fun getList(): List<T>
}
