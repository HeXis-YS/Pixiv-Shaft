package ceui.lisa.core

import ceui.lisa.models.IllustsBean
import java.util.ArrayList
import java.util.UUID

class PageData : IDWithList<IllustsBean> {

    private val uuid: String
    private var nextUrl: String?
    private val illustList: List<IllustsBean>

    constructor(illustList: List<IllustsBean>) {
        uuid = UUID.randomUUID().toString()
        nextUrl = null
        this.illustList = ArrayList(illustList)
    }

    constructor(uuid: String, nextUrl: String?, illustList: List<IllustsBean>) {
        this.uuid = uuid
        this.nextUrl = nextUrl
        this.illustList = ArrayList(illustList)
    }

    override fun getUUID(): String {
        return uuid
    }

    override fun getList(): List<IllustsBean> {
        return illustList
    }

    fun getNextUrl(): String? {
        return nextUrl
    }

    fun setNextUrl(nextUrl: String?) {
        this.nextUrl = nextUrl
    }
}
