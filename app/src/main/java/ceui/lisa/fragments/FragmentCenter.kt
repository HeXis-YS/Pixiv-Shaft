package ceui.lisa.fragments

import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import ceui.lisa.refresh.layout.SmartRefreshLayout
import ceui.lisa.R
import ceui.lisa.activities.MainActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.FragmentNewCenterBinding

class FragmentCenter : SwipeFragment<FragmentNewCenterBinding>() {
    private var pivisionFragment: FragmentPivisionHorizontal? = null

    override fun initLayout() {
        mLayoutID = R.layout.fragment_new_center
    }

    override fun initView() {
        val headParams = baseBind.head.layoutParams
        headParams.height = Shaft.statusHeight
        baseBind.head.layoutParams = headParams

        baseBind.toolbar.inflateMenu(R.menu.fragment_left)
        baseBind.toolbar.setNavigationOnClickListener { _: View? ->
            if (mActivity is MainActivity) {
                (mActivity as MainActivity).getDrawer().openDrawer(GravityCompat.START, true)
            }
        }
        baseBind.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_search) {
                TemplateActivity.startSearch(mContext)
                true
            } else {
                false
            }
        }

        baseBind.manga.clipToOutline = true
        baseBind.novel.clipToOutline = true
        baseBind.followNovels.clipToOutline = true

        baseBind.manga.setOnClickListener {
            TemplateActivity.startRecmdManga(mContext)
        }
        baseBind.novel.setOnClickListener {
            TemplateActivity.startRecmdNovel(mContext)
        }
        baseBind.followNovels.setOnClickListener {
            TemplateActivity.startNewNovels(mContext)
        }
    }

    override fun lazyData() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        pivisionFragment = FragmentPivisionHorizontal()
        transaction.add(R.id.fragment_pivision, pivisionFragment!!, "FragmentPivisionHorizontal")
        transaction.commitNowAllowingStateLoss()
    }

    override fun getSmartRefreshLayout(): SmartRefreshLayout = baseBind.refreshLayout

    fun forceRefresh() {
        pivisionFragment?.forceRefresh()
    }
}
