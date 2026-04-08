package ceui.lisa.fragments

import android.text.InputType
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.MutedTagAdapter
import ceui.lisa.core.LocalRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.models.TagsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.PixivOperate
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUIDialogBuilder

class FragmentMutedTags : LocalListFragment<FragmentBaseListBinding, TagsBean>(),
    Toolbar.OnMenuItemClickListener {

    override fun shouldLoadLocalDataAsync(): Boolean = true

    override fun repository(): LocalRepo<List<TagsBean>> {
        return object : LocalRepo<List<TagsBean>>() {
            override fun first(): List<TagsBean> = IllustNovelFilter.getMutedTags()

            override fun next(): List<TagsBean>? = null
        }
    }

    override fun adapter(): BaseAdapter<*, *> {
        return MutedTagAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                if (viewType == 1) {
                    val target = allItems[position]
                    PixivOperate.unMuteTag(target)
                    allItems.remove(target)
                    mAdapter.notifyItemRemoved(position)
                    mAdapter.notifyItemRangeChanged(position, allItems.size - position)
                    if (allItems.size == 0) {
                        mRecyclerView.visibility = View.INVISIBLE
                        emptyRela.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    fun addMutedTag(tagName: String, filterMode: Int) {
        var isExist = false
        for (tagsBean in allItems) {
            if (tagsBean.name == tagName && tagsBean.filter_mode == filterMode) {
                isExist = true
                break
            }
        }

        if (!isExist) {
            if (allItems.size == 0) {
                mRecyclerView.visibility = View.VISIBLE
                emptyRela.visibility = View.INVISIBLE
            }

            val tagsBean = TagsBean()
            tagsBean.name = tagName
            tagsBean.filter_mode = filterMode
            PixivOperate.muteTag(tagsBean)
            allItems.add(0, tagsBean)
            mAdapter.notifyItemInserted(0)
            mRecyclerView.scrollToPosition(0)
            mAdapter.notifyItemRangeChanged(0, allItems.size)
        } else {
            Common.showToast(tagName + getString(R.string.string_209))
        }
    }

    override fun showToolbar(): Boolean = false

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            if (allItems.size == 0) {
                Common.showToast(getString(R.string.string_215))
            } else {
                QMUIDialog.MessageDialogBuilder(mActivity)
                    .setTitle(getString(R.string.string_216))
                    .setMessage(getString(R.string.string_217))
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addAction(getString(R.string.string_218)) { dialog, _ -> dialog.dismiss() }
                    .addAction(
                        0,
                        getString(R.string.string_219),
                        QMUIDialogAction.ACTION_PROP_NEGATIVE
                    ) { dialog, _ ->
                        deleteAllMutedTags()
                        IllustNovelFilter.invalidateMutedTags()
                        Common.showToast(getString(R.string.string_220))
                        mAdapter.clear()
                        emptyRela.visibility = View.VISIBLE
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        } else if (item.itemId == R.id.action_add) {
            val builder = QMUIDialog.EditTextDialogBuilder(mActivity)
            builder.setTitle(getString(R.string.string_210))
                .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                .setPlaceholder(getString(R.string.string_211))
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setActionContainerOrientation(QMUIDialogBuilder.VERTICAL)
                .addAction(getString(R.string.string_212)) { dialog, _ -> dialog.dismiss() }
                .addAction(getString(R.string.string_437)) { dialog, _ ->
                    val text = builder.editText.text
                    if (!TextUtils.isEmpty(text)) {
                        addMutedTag(text.toString(), 1)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(activity, R.string.string_214, Toast.LENGTH_SHORT).show()
                    }
                }
                .addAction(getString(R.string.string_213)) { dialog, _ ->
                    val text = builder.editText.text
                    if (!TextUtils.isEmpty(text)) {
                        addMutedTag(text.toString(), 0)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(activity, R.string.string_214, Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }
        return true
    }

    private fun deleteAllMutedTags() {
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                AppDatabase.searchDao(mContext).deleteAllMutedTags()
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
    }
}
