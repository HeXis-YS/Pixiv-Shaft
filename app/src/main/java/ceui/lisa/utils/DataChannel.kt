package ceui.lisa.utils

import ceui.lisa.models.IllustsBean

object DataChannel {

    var downloadList: List<IllustsBean> = ArrayList()

    @JvmStatic
    fun get(): DataChannel = this
}
