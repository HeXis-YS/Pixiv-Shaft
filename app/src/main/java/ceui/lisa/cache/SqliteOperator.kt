package ceui.lisa.cache

class SqliteOperator : IOperate {

    override fun <T : Any> getModel(key: String, pClass: Class<T>): T? {
        return null
    }

    override fun <T> saveModel(ket: String, pT: T) {}

    override fun clearAll() {}

    override fun clear(key: String) {}
}
