package ceui.lisa.fragments

import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.activities.ImageDetailActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.UActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.DownloadedAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.DownloadEntity
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyDownloadedBinding
import ceui.lisa.interfaces.Callback
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.models.IllustsBean
import ceui.lisa.notification.DownloadReceiver
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import java.util.ArrayList

class FragmentDownloadFinish : LocalListFragment<FragmentBaseListBinding, DownloadEntity>() {
    private val all = ArrayList<IllustsBean>()
    private val filePaths = ArrayList<String?>()
    private var receiver: DownloadReceiver<*>? = null

    override fun shouldLoadLocalDataAsync(): Boolean = true

    override fun adapter(): BaseAdapter<DownloadEntity, RecyDownloadedBinding> {
        return DownloadedAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                Common.showLog(className + position + " " + allItems.size)
                if (viewType == 0) {
                    val intent = Intent(mContext, ImageDetailActivity::class.java)
                    intent.putExtra("illust", filePaths)
                    intent.putExtra("dataType", "下载详情")
                    intent.putExtra("index", position)
                    startActivity(intent)
                } else if (viewType == 1) {
                    val intent = Intent(mContext, UActivity::class.java)
                    intent.putExtra(Params.USER_ID, all[position].user.id)
                    startActivity(intent)
                } else if (viewType == 2) {
                    deleteDownloadEntity(allItems[position])
                    allItems.removeAt(position)
                    mAdapter.notifyItemRemoved(position)
                    mAdapter.notifyItemRangeChanged(position, allItems.size - position)
                }
            }
        })
    }

    override fun repository(): BaseRepo {
        return object : LocalRepo<List<DownloadEntity>>() {
            override fun first(): List<DownloadEntity> =
                AppDatabase.downloadDao(mContext).getAll(PAGE_SIZE, 0)

            override fun next(): List<DownloadEntity> =
                AppDatabase.downloadDao(mContext).getAll(PAGE_SIZE, allItems.size)

            override fun hasNext(): Boolean = true
        }
    }

    private fun deleteDownloadEntity(downloadEntity: DownloadEntity) {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.downloadDao(mContext).delete(downloadEntity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    override fun onFirstLoaded(illustHistoryEntities: List<DownloadEntity>) {
        all.clear()
        filePaths.clear()
        for (entity in illustHistoryEntities) {
            val illustsBean = Shaft.sGson.fromJson(entity.illustGson, IllustsBean::class.java)
            all.add(illustsBean)
            filePaths.add(entity.filePath)
        }
    }

    override fun onNextLoaded(illustHistoryEntities: List<DownloadEntity>) {
        for (entity in illustHistoryEntities) {
            val illustsBean = Shaft.sGson.fromJson(entity.illustGson, IllustsBean::class.java)
            Common.showLog(className + "add " + all.size + illustsBean.title)
            all.add(illustsBean)
            filePaths.add(entity.filePath)
        }
    }

    override fun showToolbar(): Boolean = false

    override fun onAdapterPrepared() {
        super.onAdapterPrepared()
        val intentFilter = IntentFilter()
        receiver = DownloadReceiver(
            Callback<DownloadEntity> { entity ->
                mRecyclerView.visibility = View.VISIBLE
                emptyRela.visibility = View.INVISIBLE
                allItems.add(0, entity)
                all.add(0, Shaft.sGson.fromJson(entity.illustGson, IllustsBean::class.java))
                filePaths.add(0, entity.filePath)
                mAdapter.notifyItemInserted(0)
                mRecyclerView.scrollToPosition(0)
                mAdapter.notifyItemRangeChanged(0, allItems.size)
            },
            DownloadReceiver.NOTIFY_FRAGMENT_DOWNLOAD_FINISH,
        )
        intentFilter.addAction(Params.DOWNLOAD_FINISH)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver!!, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver!!)
        }
    }

    override fun isLazy(): Boolean = false
}
