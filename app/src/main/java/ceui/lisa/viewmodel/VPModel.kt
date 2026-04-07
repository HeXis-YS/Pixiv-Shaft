package ceui.lisa.viewmodel

import androidx.lifecycle.ViewModel
import ceui.lisa.model.ColorItem
import ceui.lisa.utils.Common

class VPModel : ViewModel() {
    private var allPages: List<MutableList<ColorItem>> = ArrayList()

    init {
        Common.showLog("trace VPModel 构造" + allPages.size)
        val pages = ArrayList<MutableList<ColorItem>>()
        for (i in 0 until 8) {
            pages.add(ArrayList())
        }
        allPages = pages
    }

    fun getAllPages(): List<MutableList<ColorItem>> {
        return allPages
    }

    fun setAllPages(allPages: List<MutableList<ColorItem>>) {
        this.allPages = allPages
    }

    fun getRightList(index: Int): List<ColorItem> {
        return allPages[index]
    }
}
