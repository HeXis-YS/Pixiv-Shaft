package ceui.lisa.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.databinding.ViewpagerWithTablayoutBinding
import ceui.lisa.utils.MyOnTabSelectedListener

class FragmentNew : BaseFragment<ViewpagerWithTablayoutBinding>() {
    override fun initLayout() {
        mLayoutID = R.layout.viewpager_with_tablayout
    }

    override fun initView() {
        val chineseTitles = arrayOf(
            Shaft.getContext().getString(R.string.type_illust),
            Shaft.getContext().getString(R.string.type_manga),
            Shaft.getContext().getString(R.string.type_novel)
        )
        val fragments = arrayOf<Fragment>(
            FragmentLatestWorks.newInstance("illust"),
            FragmentLatestWorks.newInstance("manga"),
            FragmentLatestNovel()
        )
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }
        baseBind.toolbarTitle.setText(R.string.string_204)
        baseBind.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int): Fragment = fragments[position]

            override fun getCount(): Int = chineseTitles.size

            override fun getPageTitle(position: Int): CharSequence = chineseTitles[position]
        }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(fragments)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
    }
}
