package ceui.lisa.fragments

import android.content.IntentFilter
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.DownloadingAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.DownloadItem
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.Manager
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyDownloadTaskBinding
import ceui.lisa.interfaces.Callback
import ceui.lisa.model.Holder
import ceui.lisa.notification.DownloadReceiver
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params

class FragmentDownloading : LocalListFragment<FragmentBaseListBinding, DownloadItem>() {
    private var receiver: DownloadReceiver<*>? = null

    override fun adapter(): BaseAdapter<DownloadItem, RecyDownloadTaskBinding> =
        DownloadingAdapter(allItems, mContext)

    override fun repository(): BaseRepo {
        return object : LocalRepo<List<DownloadItem>>() {
            override fun first(): List<DownloadItem> = Manager.get().content

            override fun next(): List<DownloadItem>? = null
        }
    }

    override fun showToolbar(): Boolean = false

    override fun onAdapterPrepared() {
        super.onAdapterPrepared()
        val intentFilter = IntentFilter()
        receiver = DownloadReceiver<Holder>(Callback { holder ->
            if (holder.code == Params.DOWNLOAD_FAILED) {
                val item = holder.downloadItem
                item?.setState(DownloadItem.DownloadState.FAILED)
                mAdapter.notifyItemChanged(holder.index)
                Common.showLog("收到了失败提醒")
            } else if (holder.code == Params.DOWNLOAD_SUCCESS) {
                val position = holder.index
                if (position < allItems.size) {
                    try {
                        allItems.removeAt(position)
                        mAdapter.notifyItemRemoved(position)
                        mAdapter.notifyItemRangeChanged(position, allItems.size - position)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }

                if (allItems.size == 0) {
                    emptyRela.visibility = View.VISIBLE
                }
            }
        }, DownloadReceiver.NOTIFY_FRAGMENT_DOWNLOADING)
        intentFilter.addAction(Params.DOWNLOAD_ING)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver!!, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver!!)
        }
        Manager.get().clearCallback()
    }
}
