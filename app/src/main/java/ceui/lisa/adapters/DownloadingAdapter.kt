package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.core.DownloadItem
import ceui.lisa.core.Manager
import ceui.lisa.databinding.RecyDownloadTaskBinding
import ceui.lisa.download.DownloadHolder
import ceui.lisa.download.FileSizeUtil
import ceui.lisa.interfaces.Callback
import ceui.lisa.utils.GlideUtil
import rxhttp.wrapper.entity.Progress

class DownloadingAdapter(
    targetList: List<DownloadItem>,
    context: Context,
) : BaseAdapter<DownloadItem, RecyDownloadTaskBinding>(targetList, context) {

    override fun getNormalItem(parent: ViewGroup): ViewHolder<RecyDownloadTaskBinding> {
        return DownloadHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                mLayoutID,
                parent,
                false,
            ),
        )
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_download_task
    }

    override fun bindData(
        target: DownloadItem,
        bindView: ViewHolder<RecyDownloadTaskBinding>,
        position: Int,
    ) {
        bindView.baseBind.taskName.text = target.name
        bindView.baseBind.progress.tag = target.uuid
        if (!TextUtils.isEmpty(target.showUrl)) {
            Glide.with(mContext)
                .load(GlideUtil.getUrl(target.showUrl))
                .into(bindView.baseBind.illustImage)
        }

        val manager = Manager.get()
        manager.setCallback(target.uuid, object : Callback<Progress> {
            override fun doSomething(t: Progress) {
                if (manager.uuid == target.uuid) {
                    bindView.baseBind.progress.progress = t.progress
                    bindView.baseBind.currentSize.text = String.format(
                        "%s / %s",
                        FileSizeUtil.formatFileSize(t.currentSize),
                        FileSizeUtil.formatFileSize(t.totalSize),
                    )
                    bindView.baseBind.state.text = "正在下载"
                }
            }
        })

        setDefaultView(target, bindView, position)

        bindView.itemView.setOnClickListener { _: View ->
            if (target.isPaused()) {
                manager.startOne(target.uuid)
                bindView.baseBind.state.text = "未开始"
            } else {
                manager.stopOne(target.uuid)
                bindView.baseBind.state.text = "已暂停"
            }
        }

        bindView.baseBind.deleteItem.setOnClickListener { _: View ->
            manager.clearOne(target.uuid)
            allItems.remove(target)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, allItems.size - position)
        }
    }

    private fun setDefaultView(
        target: DownloadItem,
        bindView: ViewHolder<RecyDownloadTaskBinding>,
        position: Int,
    ) {
        bindView.baseBind.progress.progress = target.nonius
        bindView.baseBind.currentSize.text = mContext.getString(R.string.string_115)

        when (target.getState()) {
            DownloadItem.DownloadState.INIT -> bindView.baseBind.state.text = "未开始"
            DownloadItem.DownloadState.DOWNLOADING -> bindView.baseBind.state.text = "正在下载"
            DownloadItem.DownloadState.PAUSED -> bindView.baseBind.state.text = "已暂停"
            DownloadItem.DownloadState.FAILED -> bindView.baseBind.state.text = "已失败"
            DownloadItem.DownloadState.SUCCESS -> bindView.baseBind.state.text = "已完成"
            else -> bindView.baseBind.state.text = ""
        }
    }
}
