package ceui.lisa.helper

import ceui.lisa.models.Deduplicatable
import java.util.ArrayList
import java.util.HashSet

class DeduplicateArrayList<E : Deduplicatable> : ArrayList<E> {

    private var innerSet: MutableSet<Any?> = HashSet()

    constructor(initialCapacity: Int) : super(initialCapacity)

    constructor() : super()

    constructor(c: Collection<E>) : super(c) {
        val removeList = ArrayList<E>()
        for (i in 0 until size) {
            val element = get(i)
            val duplicateKey = element.duplicateKey
            if (innerSet.contains(duplicateKey)) {
                removeList.add(element)
            } else {
                innerSet.add(duplicateKey)
            }
        }
        removeAll(removeList)
    }

    override fun add(element: E): Boolean {
        val duplicateKey = element.duplicateKey
        return if (innerSet.contains(duplicateKey)) {
            true
        } else {
            innerSet.add(duplicateKey)
            super.add(element)
        }
    }

    override fun add(index: Int, element: E) {
        val duplicateKey = element.duplicateKey
        if (!innerSet.contains(duplicateKey)) {
            innerSet.add(duplicateKey)
            super.add(index, element)
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        synchronized(this) {
            val copied = ArrayList<E>(elements)
            val removeList = ArrayList<E>()
            for (i in copied.indices) {
                val element = copied[i]
                val duplicateKey = element.duplicateKey
                if (innerSet.contains(duplicateKey)) {
                    removeList.add(element)
                } else {
                    innerSet.add(duplicateKey)
                }
            }
            copied.removeAll(removeList)
            return super.addAll(copied)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        synchronized(this) {
            val copied = ArrayList<E>(elements)
            val removeList = ArrayList<E>()
            for (i in copied.indices) {
                val element = copied[i]
                val duplicateKey = element.duplicateKey
                if (innerSet.contains(duplicateKey)) {
                    removeList.add(element)
                } else {
                    innerSet.add(duplicateKey)
                }
            }
            copied.removeAll(removeList)
            return super.addAll(index, copied)
        }
    }

    override fun remove(element: E): Boolean {
        innerSet.remove(element.duplicateKey)
        return super.remove(element)
    }

    override fun removeAt(index: Int): E {
        val element = super.removeAt(index)
        innerSet.remove(element.duplicateKey)
        return element
    }

    override fun clear() {
        super.clear()
        innerSet.clear()
    }

    companion object {
        @JvmStatic
        fun <T : Deduplicatable> addAllWithNoRepeat(dist: MutableCollection<T>, src: Collection<T>) {
            val set = HashSet<Any?>()
            for (element in dist) {
                set.add(element.duplicateKey)
            }
            for (element in src) {
                if (!set.contains(element.duplicateKey)) {
                    dist.add(element)
                }
            }
        }
    }
}
