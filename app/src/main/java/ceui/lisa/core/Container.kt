package ceui.lisa.core

import ceui.lisa.utils.Common
import java.util.HashMap

class Container private constructor() {

    private val pages = HashMap<String, PageData>()
    private var isNetworking = false

    /**
     * 用 HashMap 存储，app杀掉之后就没有了
     */
    fun addPageToMap(pageData: PageData?) {
        if (pageData == null) {
            return
        }

        val uuid = pageData.getUUID()
        if (uuid.isEmpty()) {
            return
        }

        pages[uuid] = pageData
        Common.showLog("Container addPage $uuid")
    }

    fun getPage(uuid: String?): PageData? {
        Common.showLog("Container getPage $uuid")
        if (uuid.isNullOrEmpty() || pages.isEmpty()) {
            return null
        }

        return pages[uuid]
    }

    fun clear() {
        Common.showLog("Container clear ")
        if (pages.isNotEmpty()) {
            pages.clear()
        }
    }

    fun isNetworking(): Boolean {
        return isNetworking
    }

    fun setNetworking(networking: Boolean) {
        isNetworking = networking
    }

    companion object {
        private val INSTANCE = Container()

        @JvmStatic
        fun get(): Container {
            return INSTANCE
        }
    }
}
