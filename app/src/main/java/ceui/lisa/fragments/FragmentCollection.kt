package ceui.lisa.fragments

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.ViewpagerWithTablayoutBinding
import ceui.lisa.utils.MyOnTabSelectedListener
import ceui.lisa.utils.Params
import ceui.lisa.viewpager.transforms.DrawerTransformer
import java.util.Arrays
import java.util.HashSet

class FragmentCollection : BaseFragment<ViewpagerWithTablayoutBinding>() {
    private var allPages: Array<Fragment>? = null
    private var chineseTitles: Array<String>? = null
    private var type = 0

    companion object {
        private val filterType: Set<Int> = HashSet(Arrays.asList(0, 1))

        @JvmStatic
        fun newInstance(type: Int): FragmentCollection {
            val args = Bundle()
            args.putInt(Params.DATA_TYPE, type)
            val fragment = FragmentCollection()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        type = bundle.getInt(Params.DATA_TYPE)
    }

    override fun initLayout() {
        mLayoutID = R.layout.viewpager_with_tablayout
    }

    override fun initView() {
        when (type) {
            0 -> {
                allPages = arrayOf(
                    FragmentLikeIllust.newInstance(Shaft.sUserModel.user.id, Params.TYPE_PUBLIC),
                    FragmentLikeIllust.newInstance(Shaft.sUserModel.user.id, Params.TYPE_PRIVATE)
                )
                chineseTitles = arrayOf(
                    Shaft.getContext().getString(R.string.public_like_illust),
                    Shaft.getContext().getString(R.string.private_like_illust)
                )
            }
            1 -> {
                allPages = arrayOf(
                    FragmentLikeNovel.newInstance(Shaft.sUserModel.user.id, Params.TYPE_PUBLIC, false),
                    FragmentLikeNovel.newInstance(Shaft.sUserModel.user.id, Params.TYPE_PRIVATE, false)
                )
                chineseTitles = arrayOf(
                    Shaft.getContext().getString(R.string.public_like_novel),
                    Shaft.getContext().getString(R.string.private_like_novel)
                )
            }
            2 -> {
                allPages = arrayOf(
                    FragmentFollowUser.newInstance(Shaft.sUserModel.user.id, Params.TYPE_PUBLIC, false),
                    FragmentFollowUser.newInstance(Shaft.sUserModel.user.id, Params.TYPE_PRIVATE, false)
                )
                chineseTitles = arrayOf(
                    Shaft.getContext().getString(R.string.public_like_user),
                    Shaft.getContext().getString(R.string.private_like_user)
                )
            }
            3 -> {
                allPages = arrayOf(FragmentWatchlistNovel())
                chineseTitles = arrayOf(Shaft.getContext().getString(R.string.type_novel))
            }
        }

        when (type) {
            0 -> baseBind.toolbarTitle.setText(R.string.string_319)
            1 -> baseBind.toolbarTitle.setText(R.string.string_320)
            2 -> baseBind.toolbarTitle.setText(R.string.string_321)
            3 -> baseBind.toolbarTitle.setText(R.string.watchlist)
        }
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }
        baseBind.toolbar.setOnMenuItemClickListener { _: MenuItem ->
            if (baseBind.viewPager.currentItem == 0) {
                TemplateActivity.startTagFilter(mContext, type, Params.TYPE_PUBLIC)
                true
            } else if (baseBind.viewPager.currentItem == 1) {
                TemplateActivity.startTagFilter(mContext, type, Params.TYPE_PRIVATE)
                true
            } else {
                false
            }
        }
        baseBind.viewPager.setPageTransformer(true, DrawerTransformer())
        baseBind.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager, 0) {
            override fun getItem(i: Int): Fragment = allPages!![i]

            override fun getCount(): Int = chineseTitles!!.size

            override fun getPageTitle(position: Int): CharSequence = chineseTitles!![position]
        }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(allPages!!)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
        if (filterType.contains(type)) {
            baseBind.toolbar.inflateMenu(R.menu.illust_filter)
        }
        baseBind.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {
            }

            override fun onPageSelected(i: Int) {
                baseBind.toolbar.menu.clear()
                if (filterType.contains(type)) {
                    baseBind.toolbar.inflateMenu(R.menu.illust_filter)
                }
            }

            override fun onPageScrollStateChanged(i: Int) {
            }
        })
    }
}
