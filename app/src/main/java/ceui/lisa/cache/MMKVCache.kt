package ceui.lisa.cache

class MMKVCache private constructor() : IOperate, Proxy<IOperate> {

    private val operate: IOperate = create()

    override fun <T : Any> getModel(key: String, pClass: Class<T>): T? {
        return operate.getModel(key, pClass)
    }

    override fun <T> saveModel(ket: String, pT: T) {
        operate.saveModel(ket, pT)
    }

    override fun clearAll() {
        operate.clearAll()
    }

    override fun clear(key: String) {
        operate.clear(key)
    }

    override fun create(): IOperate {
        return MMKVOperator()
    }

    companion object {
        private val INSTANCE = MMKVCache()

        @JvmStatic
        fun get(): MMKVCache = INSTANCE
    }
}
