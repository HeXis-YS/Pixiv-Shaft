package ceui.lisa.core

import ceui.lisa.utils.Common

object TimeRecord {

    @JvmField
    var startTime: Long = 0L

    @JvmField
    var endTime: Long = 0L

    @JvmStatic
    fun start() {
        startTime = 0L
        Common.showLog("TimeRecord start " + System.nanoTime())
        startTime = System.nanoTime()
    }

    @JvmStatic
    fun end() {
        endTime = 0L
        Common.showLog("TimeRecord end " + System.nanoTime())
        endTime = System.nanoTime()
        result()
    }

    @JvmStatic
    fun result() {
        val temp = endTime - startTime
        Common.showLog("ScrollReceiver广播 TimeRecord result 毫秒：" + temp / 1000000L)
    }
}
