package ceui.lisa.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.databinding.ViewpagerWithTablayoutBinding
import ceui.lisa.utils.MyOnTabSelectedListener
import ceui.lisa.utils.Params

class FragmentViewPager : BaseFragment<ViewpagerWithTablayoutBinding>() {
    private var title: String? = null
    private var mFragments: Array<ListFragment<*, *>>? = null

    companion object {
        @JvmStatic
        fun newInstance(title: String?): FragmentViewPager {
            val args = Bundle()
            args.putString(Params.TITLE, title)
            val fragment = FragmentViewPager()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        title = bundle.getString(Params.TITLE)
    }

    override fun initLayout() {
        mLayoutID = R.layout.viewpager_with_tablayout
    }

    override fun initView() {
        if (TextUtils.equals(title, Params.VIEW_PAGER_MUTED)) {
            val chineseTitles = arrayOf(
                Shaft.getContext().getString(R.string.string_353),
                Shaft.getContext().getString(R.string.string_381),
                Shaft.getContext().getString(R.string.string_354)
            )
            mFragments = arrayOf(
                FragmentMutedTags(),
                FragmentMutedUser(),
                FragmentMutedObjects()
            )
            baseBind.toolbar.inflateMenu(R.menu.delete_and_add)
            baseBind.toolbar.setOnMenuItemClickListener(mFragments!![0] as Toolbar.OnMenuItemClickListener)
            baseBind.toolbarTitle.setText(R.string.muted_history)
            baseBind.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment = mFragments!![position]

                override fun getCount(): Int = chineseTitles.size

                override fun getPageTitle(position: Int): CharSequence = chineseTitles[position]
            }
            baseBind.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    baseBind.toolbar.setOnMenuItemClickListener(mFragments!![position] as Toolbar.OnMenuItemClickListener)
                    if (position == 0) {
                        baseBind.toolbar.menu.clear()
                        baseBind.toolbar.inflateMenu(R.menu.delete_and_add)
                    } else {
                        baseBind.toolbar.menu.clear()
                        baseBind.toolbar.inflateMenu(R.menu.delete_muted_history)
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })
        } else if (TextUtils.equals(title, Params.VIEW_PAGER_R18)) {
            baseBind.appBar.setPadding(0, Shaft.statusHeight, 0, 0)
            baseBind.toolbar.visibility = View.GONE
            val chineseTitles = arrayOf(
                Shaft.getContext().getString(R.string.r_eighteen),
                Shaft.getContext().getString(R.string.r_eighteen_weekly_rank),
                Shaft.getContext().getString(R.string.r_eighteen_male_rank),
                Shaft.getContext().getString(R.string.r_eighteen_female_rank),
                Shaft.getContext().getString(R.string.r_eighteen_ai_rank)
            )
            mFragments = arrayOf(
                FragmentRankIllust.newInstance(8, "", false),
                FragmentRankIllust.newInstance(9, "", false),
                FragmentRankIllust.newInstance(10, "", false),
                FragmentRankIllust.newInstance(11, "", false),
                FragmentRankIllust.newInstance(12, "", false)
            )
            baseBind.toolbarTitle.setText(R.string.string_r)
            baseBind.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment = mFragments!![position]

                override fun getCount(): Int = chineseTitles.size

                override fun getPageTitle(position: Int): CharSequence = chineseTitles[position]
            }
        }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(mFragments as Array<Fragment>)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }
    }

    fun forceRefresh() {
        try {
            mFragments?.get(baseBind.viewPager.currentItem)?.forceRefresh()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
