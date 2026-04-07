package ceui.lisa.core

import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.interfaces.ListShow
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.NovelBean
import ceui.loxia.ObjectPool
import io.reactivex.functions.Function

open class Mapper<T : ListShow<*>> : Function<T, T> {

    override fun apply(t: T): T {
        val dash = ArrayList<Any>()
        for (item in t.list) {
            when (item) {
                is IllustsBean -> {
                    val isTagBanned = IllustNovelFilter.judgeTag(item)
                    val isIdBanned = IllustNovelFilter.judgeID(item)
                    val isUserBanned = IllustNovelFilter.judgeUserID(item)
                    val isR18FilterBanned = IllustNovelFilter.judgeR18Filter(item)
                    if (isTagBanned || isIdBanned || isUserBanned || isR18FilterBanned) {
                        dash.add(item)
                    }
                    ObjectPool.updateIllust(item)
                }

                is NovelBean -> {
                    val isTagBanned = IllustNovelFilter.judgeTag(item)
                    val isIdBanned = IllustNovelFilter.judgeID(item)
                    val isUserBanned = IllustNovelFilter.judgeUserID(item)
                    val isR18FilterBanned = IllustNovelFilter.judgeR18Filter(item)
                    if (isTagBanned || isIdBanned || isUserBanned || isR18FilterBanned) {
                        dash.add(item)
                    }
                }
            }
        }

        if (dash.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            (t.list as MutableList<Any>).removeAll(dash)
        }
        return t
    }
}
