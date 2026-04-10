package ceui.lisa.fragments

import androidx.viewbinding.ViewBinding

abstract class BaseLazyFragment<T : ViewBinding> : BaseFragment<T>() {
    @JvmField
    protected var isLoaded = false

    open fun lazyData() {}

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        shouldLoadData()
    }

    override fun initData() {
        shouldLoadData()
    }

    fun shouldLoadData() {
        if (!isInit) {
            return
        }

        if (userVisibleHint && isLazy() && !isLoaded) {
            lazyData()
            isLoaded = true
        }
    }

    open fun isLazy(): Boolean = true
}
