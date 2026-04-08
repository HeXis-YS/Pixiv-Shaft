package ceui.lisa.fragments

import androidx.databinding.ViewDataBinding
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.ColorAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.LocalRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.model.ColorItem

class FragmentColors : LocalListFragment<FragmentBaseListBinding, ColorItem>() {
    companion object {
        @JvmField
        val COLOR_NAME_CODES = intArrayOf(
            R.string.color_shiYinPurple,
            R.string.color_classicBlue,
            R.string.color_officialBlue,
            R.string.color_scallionGreen,
            R.string.color_summerYellow,
            R.string.color_peachPink,
            R.string.color_activeRed,
            R.string.color_classicPurple,
            R.string.color_classicGreen,
            R.string.color_girlPink
        )
    }

    override fun adapter(): BaseAdapter<*, out ViewDataBinding> = ColorAdapter(allItems, mContext)

    override fun repository(): BaseRepo {
        return object : LocalRepo<List<ColorItem>>() {
            override fun first(): List<ColorItem> = getList()

            override fun next(): List<ColorItem>? = null
        }
    }

    fun getList(): List<ColorItem> {
        val itemList = ArrayList<ColorItem>()
        val current = Shaft.sSettings.themeIndex
        itemList.add(ColorItem(0, getString(COLOR_NAME_CODES[0]), "#686bdd", current == 0))
        itemList.add(ColorItem(1, getString(COLOR_NAME_CODES[1]), "#56baec", current == 1))
        itemList.add(ColorItem(2, getString(COLOR_NAME_CODES[2]), "#008BF3", current == 2))
        itemList.add(ColorItem(3, getString(COLOR_NAME_CODES[3]), "#03d0bf", current == 3))
        itemList.add(ColorItem(4, getString(COLOR_NAME_CODES[4]), "#fee65e", current == 4))
        itemList.add(ColorItem(5, getString(COLOR_NAME_CODES[5]), "#fe83a2", current == 5))
        itemList.add(ColorItem(6, getString(COLOR_NAME_CODES[6]), "#f44336", current == 6))
        itemList.add(ColorItem(7, getString(COLOR_NAME_CODES[7]), "#673AB7", current == 7))
        itemList.add(ColorItem(8, getString(COLOR_NAME_CODES[8]), "#4CAF50", current == 8))
        itemList.add(ColorItem(9, getString(COLOR_NAME_CODES[9]), "#E91E63", current == 9))
        return itemList
    }

    override fun getToolbarTitle(): String = getString(R.string.string_324)
}
