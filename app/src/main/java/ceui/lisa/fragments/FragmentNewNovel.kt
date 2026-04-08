package ceui.lisa.fragments

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.ViewpagerWithTablayoutBinding
import ceui.lisa.utils.MyOnTabSelectedListener
import ceui.lisa.utils.Params

class FragmentNewNovel : BaseFragment<ViewpagerWithTablayoutBinding>() {
    override fun initLayout() {
        mLayoutID = R.layout.viewpager_with_tablayout
    }

    override fun initView() {
        val titles = arrayOf(
            Shaft.getContext().getString(R.string.recommend_illust),
            Shaft.getContext().getString(R.string.hot_tag)
        )
        val fragments = arrayOf<Fragment>(
            FragmentRecmdNovel(),
            FragmentHotTag.newInstance(Params.TYPE_NOVEL)
        )
        baseBind.toolbarTitle.setText(R.string.type_novel)
        baseBind.toolbar.setNavigationOnClickListener { finish() }
        baseBind.toolbar.inflateMenu(R.menu.fragment_left)
        baseBind.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_search) {
                TemplateActivity.startSearch(mContext)
                true
            } else {
                false
            }
        }
        baseBind.viewPager.adapter =
            object : FragmentPagerAdapter(childFragmentManager, 0) {
                override fun getItem(i: Int): Fragment = fragments[i]

                override fun getCount(): Int = titles.size

                override fun getPageTitle(position: Int): CharSequence = titles[position]
            }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(fragments)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
    }
}
