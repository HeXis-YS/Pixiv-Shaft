package ceui.lisa.fragments

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.SimpleUserAdapter
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.MuteEntity
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecySimpleUserBinding
import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.models.UserBean
import ceui.lisa.utils.Common
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction

class FragmentMutedUser : LocalListFragment<FragmentBaseListBinding, UserBean>(),
    Toolbar.OnMenuItemClickListener {

    override fun shouldLoadLocalDataAsync(): Boolean = true

    override fun repository(): LocalRepo<List<UserBean>> {
        return object : LocalRepo<List<UserBean>>() {
            override fun first(): List<UserBean> {
                val entityList: List<MuteEntity> =
                    AppDatabase.searchDao(Shaft.getContext()).getMutedUser(PAGE_SIZE, 0)
                val userBeanList = ArrayList<UserBean>()
                for (muteEntity in entityList) {
                    val userBean = Shaft.sGson.fromJson(muteEntity.tagJson, UserBean::class.java)
                    userBeanList.add(userBean)
                }
                return userBeanList
            }

            override fun next(): List<UserBean> {
                val entityList: List<MuteEntity> =
                    AppDatabase.searchDao(Shaft.getContext()).getMutedUser(PAGE_SIZE, allItems.size)
                val userBeanList = ArrayList<UserBean>()
                for (muteEntity in entityList) {
                    val userBean = Shaft.sGson.fromJson(muteEntity.tagJson, UserBean::class.java)
                    userBeanList.add(userBean)
                }
                return userBeanList
            }

            override fun hasNext(): Boolean = true
        }
    }

    override fun adapter(): BaseAdapter<UserBean, RecySimpleUserBinding> =
        SimpleUserAdapter(allItems, mContext, true)

    override fun showToolbar(): Boolean = false

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            if (allItems.size == 0) {
                Common.showToast(getString(R.string.string_388))
            } else {
                QMUIDialog.MessageDialogBuilder(mActivity)
                    .setTitle(getString(R.string.string_216))
                    .setMessage(getString(R.string.string_389))
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addAction(getString(R.string.string_218)) { dialog, _ -> dialog.dismiss() }
                    .addAction(
                        0,
                        getString(R.string.string_219),
                        QMUIDialogAction.ACTION_PROP_NEGATIVE
                    ) { dialog, _ ->
                        deleteAllMutedUsers()
                        IllustNovelFilter.invalidateMutedUsers()
                        Common.showToast(getString(R.string.string_220))
                        mAdapter.clear()
                        emptyRela.visibility = android.view.View.VISIBLE
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
        return true
    }

    private fun deleteAllMutedUsers() {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.searchDao(mContext).deleteAllMutedUsers()
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }
}
