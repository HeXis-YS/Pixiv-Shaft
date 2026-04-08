package ceui.lisa.fragments

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ToxicBakery.viewpager.transforms.DrawerTransformer
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.core.Manager
import ceui.lisa.database.AppDatabase
import ceui.lisa.databinding.ViewpagerWithTablayoutBinding
import ceui.lisa.utils.Common
import ceui.lisa.utils.MyOnTabSelectedListener

class FragmentDownload : BaseFragment<ViewpagerWithTablayoutBinding>() {
    private val allPages: Array<Fragment> = arrayOf(FragmentDownloading(), FragmentDownloadFinish())

    override fun initLayout() {
        mLayoutID = R.layout.viewpager_with_tablayout
    }

    override fun initView() {
        val chineseTitles = arrayOf(
            Shaft.getContext().getString(R.string.now_downloading),
            Shaft.getContext().getString(R.string.has_download),
        )
        baseBind.toolbarTitle.setText(R.string.string_203)
        baseBind.toolbar.inflateMenu(R.menu.start_all)
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }
        baseBind.toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.action_delete) {
                    if (allPages[1] is FragmentDownloadFinish &&
                        (allPages[1] as FragmentDownloadFinish).count > 0
                    ) {
                        QMUIDialog.MessageDialogBuilder(mActivity)
                            .setTitle("提示")
                            .setMessage("这将会删除所有的下载记录，但是已下载的文件不会被删除")
                            .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction(
                                0,
                                "删除",
                                QMUIDialogAction.ACTION_PROP_NEGATIVE,
                            ) { dialog, _ ->
                                AppDatabase.downloadDao(mContext).deleteAllDownload()
                                (allPages[1] as FragmentDownloadFinish).clearAndRefresh()
                                Common.showToast("下载记录清除成功")
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        Common.showToast("没有可删除的记录")
                    }
                    return true
                } else if (item.itemId == R.id.action_start) {
                    Manager.get().startAll()
                    if (allPages[0] is FragmentDownloading) {
                        val adapter: BaseAdapter<*, *>? = (allPages[0] as FragmentDownloading).mAdapter
                        adapter?.notifyDataSetChanged()
                    }
                } else if (item.itemId == R.id.action_stop) {
                    Manager.get().stopAll()
                    if (allPages[0] is FragmentDownloading) {
                        val adapter: BaseAdapter<*, *>? = (allPages[0] as FragmentDownloading).mAdapter
                        adapter?.notifyDataSetChanged()
                    }
                } else if (item.itemId == R.id.action_clear) {
                    if (allPages[0] is FragmentDownloading &&
                        (allPages[0] as FragmentDownloading).count > 0
                    ) {
                        QMUIDialog.MessageDialogBuilder(mActivity)
                            .setTitle("提示")
                            .setMessage("清空所有未完成的任务吗？")
                            .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                            .addAction("取消") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction(
                                0,
                                "清空",
                                QMUIDialogAction.ACTION_PROP_NEGATIVE,
                            ) { dialog, _ ->
                                Manager.get().clearAll()
                                (allPages[0] as FragmentDownloading).clearAndRefresh()
                                Common.showToast("下载任务清除成功")
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        Common.showToast("没有可删除的记录")
                    }
                }
                return false
            }
        })
        baseBind.viewPager.setPageTransformer(true, DrawerTransformer())
        baseBind.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(i: Int): Fragment = allPages[i]

            override fun getCount(): Int = chineseTitles.size

            override fun getPageTitle(position: Int): CharSequence = chineseTitles[position]
        }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(allPages)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
        baseBind.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {
            }

            override fun onPageSelected(i: Int) {
                if (i == 0) {
                    baseBind.toolbar.menu.clear()
                    baseBind.toolbar.inflateMenu(R.menu.start_all)
                } else {
                    baseBind.toolbar.menu.clear()
                    baseBind.toolbar.inflateMenu(R.menu.delete_all)
                }
            }

            override fun onPageScrollStateChanged(i: Int) {
            }
        })
    }
}
