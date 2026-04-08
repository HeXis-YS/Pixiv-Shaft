package ceui.lisa.fragments

import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.UActivity
import ceui.lisa.activities.VActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.HistoryAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.Container
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.PageData
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.IllustHistoryEntity
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyViewHistoryBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.viewmodel.BaseModel
import ceui.lisa.viewmodel.HistoryModel
import ceui.loxia.ObjectPool
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction

class FragmentHistory : LocalListFragment<FragmentBaseListBinding, IllustHistoryEntity>() {
    override fun shouldLoadLocalDataAsync(): Boolean = true

    override fun adapter(): BaseAdapter<IllustHistoryEntity, RecyViewHistoryBinding> {
        return HistoryAdapter(allItems, mContext).setOnItemClickListener(
            OnItemClickListener { v, position, viewType ->
                Common.showLog(className + position + " " + allItems.size)
                when (viewType) {
                    0 -> {
                        val allImages = (mModel as HistoryModel).getAll()
                        if (!Common.isEmpty(allImages)) {
                            val pageData = PageData(allImages)
                            Container.get().addPageToMap(pageData)

                            val historyEntity = allItems[position]
                            var index = 0
                            for (i in allImages.indices) {
                                if (allImages[i].id == historyEntity.illustID) {
                                    index = i
                                    break
                                }
                            }

                            val intent = Intent(mContext, VActivity::class.java)
                            intent.putExtra(Params.POSITION, index)
                            intent.putExtra(Params.PAGE_UUID, pageData.getUUID())
                            mContext.startActivity(intent)
                        }
                    }

                    1 -> {
                        val intent = Intent(mContext, UActivity::class.java)
                        intent.putExtra(Params.USER_ID, v!!.tag as Int)
                        mContext.startActivity(intent)
                    }

                    2 -> {
                        QMUIDialog.MessageDialogBuilder(mActivity)
                            .setTitle(getString(R.string.string_143))
                            .setMessage(getString(R.string.string_352))
                            .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                            .addAction(getString(R.string.string_142)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .addAction(
                                0,
                                getString(R.string.string_141),
                                QMUIDialogAction.ACTION_PROP_NEGATIVE,
                            ) { dialog, _ ->
                                deleteHistoryEntity(allItems[position])
                                allItems.removeAt(position)
                                mAdapter.notifyItemRemoved(position)
                                mAdapter.notifyItemRangeChanged(position, allItems.size - position)
                                if (allItems.size == 0) {
                                    mRecyclerView.visibility = View.INVISIBLE
                                    emptyRela.visibility = View.VISIBLE
                                }
                                Common.showToast(getString(R.string.string_220))
                                dialog.dismiss()
                            }.show()
                    }
                }
            },
        )
    }

    private fun deleteHistoryEntity(historyEntity: IllustHistoryEntity) {
        RxRun.runOn(
            object : RxRunnable<Void>() {
                override fun execute(): Void {
                    AppDatabase.downloadDao(mContext).delete(historyEntity)
                    @Suppress("NULL_FOR_NONNULL_TYPE")
                    return null as Void
                }
            },
            TryCatchObserverImpl(),
        )
    }

    override fun repository(): BaseRepo {
        return object : LocalRepo<List<IllustHistoryEntity>>() {
            override fun first(): List<IllustHistoryEntity> =
                AppDatabase.downloadDao(mContext).getAllViewHistory(PAGE_SIZE, 0)

            override fun next(): List<IllustHistoryEntity> =
                AppDatabase.downloadDao(mContext).getAllViewHistory(PAGE_SIZE, allItems.size)

            override fun hasNext(): Boolean = true
        }
    }

    override fun onFirstLoaded(illustHistoryEntities: List<IllustHistoryEntity>) {
        val allImages = (mModel as HistoryModel).getAll() as MutableList<IllustsBean>
        allImages.clear()
        for (entity in illustHistoryEntities) {
            if (entity.type == 0) {
                val illustsBean = Shaft.sGson.fromJson(entity.illustJson, IllustsBean::class.java)
                ObjectPool.updateIllust(illustsBean)
                allImages.add(illustsBean)
            }
        }
    }

    override fun onNextLoaded(illustHistoryEntities: List<IllustHistoryEntity>) {
        val allImages = (mModel as HistoryModel).getAll() as MutableList<IllustsBean>
        for (entity in illustHistoryEntities) {
            if (entity.type == 0) {
                val illustsBean = Shaft.sGson.fromJson(entity.illustJson, IllustsBean::class.java)
                ObjectPool.updateIllust(illustsBean)
                allImages.add(illustsBean)
            }
        }
    }

    override fun initToolbar(toolbar: Toolbar) {
        super.initToolbar(toolbar)
        toolbar.inflateMenu(R.menu.delete_all)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_delete) {
                if (Common.isEmpty(allItems)) {
                    Common.showToast(getString(R.string.string_254))
                } else {
                    QMUIDialog.MessageDialogBuilder(mActivity)
                        .setTitle(getString(R.string.string_143))
                        .setMessage(getString(R.string.string_255))
                        .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                        .addAction(getString(R.string.string_142)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .addAction(
                            0,
                            getString(R.string.string_141),
                            QMUIDialogAction.ACTION_PROP_NEGATIVE,
                        ) { dialog, _ ->
                            AppDatabase.downloadDao(mContext).deleteAllHistory()
                            Common.showToast(getString(R.string.string_220))
                            dialog.dismiss()
                            mAdapter.clear()
                            emptyRela.visibility = View.VISIBLE
                        }.show()
                }
            }
            true
        }
    }

    override fun modelClass(): Class<out BaseModel<*>> = HistoryModel::class.java

    override fun getToolbarTitle(): String = "浏览记录"
}
