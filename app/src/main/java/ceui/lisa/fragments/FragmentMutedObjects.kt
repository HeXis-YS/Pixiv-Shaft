package ceui.lisa.fragments

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.MuteWorksAdapter
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.MuteEntity
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyViewHistoryBinding
import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.utils.Common
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction

class FragmentMutedObjects : LocalListFragment<FragmentBaseListBinding, MuteEntity>(),
    Toolbar.OnMenuItemClickListener {

    override fun shouldLoadLocalDataAsync(): Boolean = true

    override fun repository(): LocalRepo<List<MuteEntity>> {
        return object : LocalRepo<List<MuteEntity>>() {
            override fun first(): List<MuteEntity> = IllustNovelFilter.getMutedWorks()

            override fun next(): List<MuteEntity>? = null
        }
    }

    override fun adapter(): BaseAdapter<MuteEntity, RecyViewHistoryBinding> {
        return MuteWorksAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                if (viewType == 2) {
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
                            deleteMuteEntity(allItems[position])
                            IllustNovelFilter.invalidateMutedWorks()
                            allItems.removeAt(position)
                            mAdapter.notifyItemRemoved(position)
                            mAdapter.notifyItemRangeChanged(position, allItems.size - position)
                            if (allItems.isEmpty()) {
                                mRecyclerView.visibility = View.INVISIBLE
                                emptyRela.visibility = View.VISIBLE
                            }
                            Common.showToast(getString(R.string.string_220))
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        })
    }

    private fun deleteMuteEntity(muteEntity: MuteEntity) {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.searchDao(mContext).deleteMuteEntity(muteEntity)
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }

    override fun showToolbar(): Boolean = false

    override fun onMenuItemClick(item: MenuItem): Boolean = false
}
