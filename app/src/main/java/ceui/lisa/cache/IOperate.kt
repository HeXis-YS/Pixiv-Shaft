package ceui.lisa.cache

interface IOperate {

    fun <T : Any> getModel(key: String, pClass: Class<T>): T?

    fun <T> saveModel(ket: String, pT: T)

    fun clearAll()

    fun clear(key: String)
}
