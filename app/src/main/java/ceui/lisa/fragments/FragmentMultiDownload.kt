package ceui.lisa.fragments

import android.net.Uri
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import ceui.lisa.R
import ceui.lisa.activities.BaseActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.MultiDownloadAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.LocalRepo
import ceui.lisa.databinding.FragmentMultiDownloadBinding
import ceui.lisa.databinding.RecyMultiDownloadBinding
import ceui.lisa.download.IllustDownload
import ceui.lisa.feature.worker.BatchStarTask
import ceui.lisa.feature.worker.Worker
import ceui.lisa.interfaces.Callback
import ceui.lisa.interfaces.FeedBack
import ceui.lisa.interfaces.OnItemLongClickListener
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.DataChannel
import ceui.lisa.utils.DensityUtil
import ceui.lisa.view.DownloadItemDecoration
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import gdut.bsx.share2.Share2
import gdut.bsx.share2.ShareContentType

class FragmentMultiDownload : LocalListFragment<FragmentMultiDownloadBinding, IllustsBean>() {
    private var dragSelectTouchListener: DragSelectTouchListener? = null
    private var dragStartCheckStatus = false

    override fun initLayout() {
        mLayoutID = R.layout.fragment_multi_download
    }

    override fun initView() {
        super.initView()
        baseBind.toolbar.inflateMenu(R.menu.download_menu)
        baseBind.toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_1 -> {
                    for (i in allItems.indices) {
                        if (!allItems[i].isChecked) {
                            allItems[i].isChecked = true
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }

                R.id.action_2 -> {
                    for (i in allItems.indices) {
                        if (allItems[i].isChecked) {
                            allItems[i].isChecked = false
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }

                R.id.action_3 -> {
                    val content = StringBuilder()
                    for (illustsBean in allItems) {
                        if (illustsBean.isChecked) {
                            if (illustsBean.page_count == 1) {
                                content.append(illustsBean.meta_single_page.original_image_url)
                                content.append("\n")
                            } else {
                                for (i in 0 until illustsBean.page_count) {
                                    content.append(illustsBean.meta_pages[i].image_urls.original)
                                    content.append("\n")
                                }
                            }
                        }
                    }
                    val result = content.toString()
                    if (TextUtils.isEmpty(result)) {
                        Common.showToast("没有选择任何作品")
                    } else {
                        IllustDownload.downloadFile(
                            mContext as BaseActivity<*>,
                            System.currentTimeMillis().toString() + "_download_tasks.txt",
                            result,
                            Callback { t: Uri ->
                                Share2.Builder(mActivity)
                                    .setContentType(ShareContentType.FILE)
                                    .setShareFileUri(t)
                                    .setTitle("Share File")
                                    .build()
                                    .shareBySystem()
                            },
                        )
                    }
                }

                R.id.action_4 -> {
                    for (allItem in allItems) {
                        val task = BatchStarTask(allItem.user.name, allItem.id, 0)
                        Worker.get().addTask(task)
                    }
                    Worker.get().setFinalFeedBack(object : FeedBack {
                        override fun doSomething() {
                            Common.showToast("全部收藏成功")
                        }
                    })
                    Worker.get().start()
                }

                R.id.action_5 -> {
                    for (allItem in allItems) {
                        val task = BatchStarTask(allItem.user.name, allItem.id, 1)
                        Worker.get().addTask(task)
                    }
                    Worker.get().setFinalFeedBack(object : FeedBack {
                        override fun doSomething() {
                            Common.showToast("全部取消收藏成功")
                        }
                    })
                    Worker.get().start()
                }

                R.id.action_6 -> {
                    for (i in allItems.indices) {
                        allItems[i].isChecked = allItems[i].isIs_bookmarked
                    }
                    mAdapter.notifyDataSetChanged()
                }

                R.id.action_7 -> {
                    for (i in allItems.indices) {
                        allItems[i].isChecked = !Common.isIllustDownloaded(allItems[i])
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
            false
        })
        baseBind.startDownload.setOnClickListener {
            IllustDownload.downloadCheckedIllustAllPages(allItems, mContext as BaseActivity<*>)
        }

        val receiver = MyReceiver()
        dragSelectTouchListener = DragSelectTouchListener.Companion.create(mContext, receiver, null)
        baseBind.recyclerView.addOnItemTouchListener(dragSelectTouchListener!!)

        val itemAnimator: RecyclerView.ItemAnimator? = mRecyclerView.itemAnimator
        if (itemAnimator != null) {
            itemAnimator.changeDuration = 0
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private inner class MyReceiver : DragSelectReceiver {
        override fun getItemCount(): Int = allItems.size

        override fun isIndexSelectable(i: Int): Boolean = true

        override fun isSelected(i: Int): Boolean = allItems[i].isChecked

        override fun setSelected(i: Int, b: Boolean) {
            var selected = b
            if (dragStartCheckStatus) {
                selected = !selected
            }
            allItems[i].isChecked = selected
            mAdapter.notifyItemChanged(i)
        }
    }

    override fun adapter(): BaseAdapter<IllustsBean, RecyMultiDownloadBinding> {
        val adapter = MultiDownloadAdapter(allItems, mContext)
        adapter.setCallback(Callback { _: Any? ->
            baseBind.toolbarTitle.text = getToolbarTitle()
        })
        adapter.setOnItemLongClickListener(OnItemLongClickListener { _: View?, position: Int, _: Int ->
                dragStartCheckStatus = allItems[position].isChecked
                dragSelectTouchListener?.setIsActive(true, position)
            })
        return adapter
    }

    override fun repository(): BaseRepo {
        return object : LocalRepo<List<IllustsBean>>() {
            override fun first(): List<IllustsBean> = DataChannel.get().downloadList

            override fun next(): List<IllustsBean>? = null
        }
    }

    override fun initRecyclerView() {
        val manager = GridLayoutManager(mContext, 3)
        mRecyclerView.layoutManager = manager
        mRecyclerView.addItemDecoration(DownloadItemDecoration(2, DensityUtil.dp2px(1.0f)))
    }

    override fun getToolbarTitle(): String {
        if (Common.isEmpty(allItems)) {
            return getString(R.string.string_221)
        }
        var selectCount = 0
        var fileCount = 0
        for (i in allItems.indices) {
            if (allItems[i].isChecked) {
                fileCount += allItems[i].page_count
                selectCount++
            }
        }
        return selectCount.toString() + getString(R.string.string_222) + fileCount + getString(R.string.string_223)
    }

    override fun initData() {
        super.initData()
        baseBind.refreshLayout.setEnableRefresh(false)
        baseBind.refreshLayout.setEnableLoadMore(false)
    }

    override fun onFirstLoaded(illustsBeans: List<IllustsBean>) {
        initToolbar(baseBind.toolbar)
    }
}
