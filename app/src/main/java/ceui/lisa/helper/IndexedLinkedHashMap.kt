package ceui.lisa.helper

import java.util.HashMap
import java.util.LinkedHashMap

class IndexedLinkedHashMap<K, V> : LinkedHashMap<K, V>() {

    private var index: HashMap<Int, K> = HashMap()
    private var curr = 0

    override fun put(key: K, value: V): V? {
        val result = super.put(key, value)
        index[curr++] = key
        return result
    }

    fun tidyIndexes(): IndexedLinkedHashMap<K, V> {
        curr = 0
        index.clear()
        for (key in keys) {
            index[curr++] = key
        }
        return this
    }

    fun getIndexed(i: Int): V? {
        val key = index[i] ?: return null
        return super.get(key)
    }
}
