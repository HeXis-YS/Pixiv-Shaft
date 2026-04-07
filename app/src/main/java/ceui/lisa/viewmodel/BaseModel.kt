package ceui.lisa.viewmodel

import androidx.lifecycle.ViewModel
import ceui.lisa.core.BaseRepo
import ceui.lisa.helper.AppLevelViewModelHelper
import ceui.lisa.utils.Common

open class BaseModel<T> : ViewModel() {
    protected var contentList: MutableList<T>? = null
    private var isLoaded = false
    private var mBaseRepo: BaseRepo? = null

    init {
        Common.showLog("trace 构造 000")
    }

    open fun getContent(): MutableList<T> {
        if (contentList == null) {
            contentList = ArrayList()
        }
        return contentList!!
    }

    fun load(list: List<T>, isFresh: Boolean) {
        if (isFresh) {
            getContent().clear()
        }
        getContent().addAll(list)
        isLoaded = true
    }

    fun load(list: List<T>, index: Int) {
        getContent().addAll(index, list)
    }

    fun isLoaded(): Boolean {
        return isLoaded
    }

    fun getBaseRepo(): BaseRepo? {
        return mBaseRepo
    }

    fun setBaseRepo(baseRepo: BaseRepo?) {
        mBaseRepo = baseRepo
    }

    fun tidyAppViewModel() {
        tidyAppViewModel(contentList)
    }

    fun tidyAppViewModel(list: List<T>?) {
        extracted(list)
    }

    private fun extracted(list: List<T>?) {
        if (list != null) {
            AppLevelViewModelHelper.fill(list)
        }
    }
}
