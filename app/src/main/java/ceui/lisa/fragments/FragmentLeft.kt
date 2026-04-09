package ceui.lisa.fragments

import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import ceui.lisa.R
import ceui.lisa.activities.MainActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.FragmentLeftBinding
import ceui.lisa.utils.MyOnTabSelectedListener
import ceui.lisa.utils.Params

class FragmentLeft : BaseLazyFragment<FragmentLeftBinding>() {
    private var fragments: Array<Fragment>? = null

    override fun initLayout() {
        mLayoutID = R.layout.fragment_left
    }

    override fun initView() {
        val headParams = baseBind.head.layoutParams
        headParams.height = Shaft.statusHeight
        baseBind.head.layoutParams = headParams

        baseBind.toolbar.setNavigationOnClickListener {
            if (mActivity is MainActivity) {
                (mActivity as MainActivity).getDrawer().openDrawer(GravityCompat.START, true)
            }
        }
        baseBind.toolbarTitle.setText(R.string.string_207)
        baseBind.toolbar.inflateMenu(R.menu.fragment_left)
        baseBind.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_search) {
                TemplateActivity.startSearch(mContext)
                true
            } else {
                false
            }
        }
    }

    override fun lazyData() {
        val titles = arrayOf(
            Shaft.getContext().getString(R.string.recommend_illust),
            Shaft.getContext().getString(R.string.hot_tag)
        )
        fragments = arrayOf(
            FragmentRecmdIllust.newInstance("插画"),
            FragmentHotTag.newInstance(Params.TYPE_ILLUST)
        )
        baseBind.viewPager.adapter =
            object : FragmentPagerAdapter(childFragmentManager, 0) {
                override fun getItem(i: Int): Fragment = fragments!![i]

                override fun getCount(): Int = titles.size

                override fun getPageTitle(position: Int): CharSequence = titles[position]
            }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(fragments!!)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
    }

    fun forceRefresh() {
        try {
            (fragments?.get(baseBind.viewPager.currentItem) as? NetListFragment<*, *, *>)?.forceRefresh()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
