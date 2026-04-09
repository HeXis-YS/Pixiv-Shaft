package ceui.lisa.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.SAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.databinding.FragmentSelectTagBinding
import ceui.lisa.databinding.RecySelectTagBinding
import ceui.lisa.http.ErrorCtrl
import ceui.lisa.http.Retro
import ceui.lisa.model.ListBookmarkTag
import ceui.lisa.models.NullResponse
import ceui.lisa.models.TagsBean
import ceui.lisa.repo.SelectTagRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FragmentSB : NetListFragment<FragmentSelectTagBinding, ListBookmarkTag, TagsBean>() {
    private var illustID = 0
    private var lastClass = ""
    private var type = Params.TYPE_ILLUST
    private var tagNames: List<String> = emptyList()

    companion object {
        @JvmStatic
        fun newInstance(illustID: Int): FragmentSB {
            val args = Bundle()
            args.putInt(Params.ILLUST_ID, illustID)
            return FragmentSB().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(illustID: Int, tagNames: Array<String>?): FragmentSB {
            val args = Bundle()
            args.putInt(Params.ILLUST_ID, illustID)
            args.putStringArray(Params.TAG_NAMES, tagNames)
            return FragmentSB().apply {
                arguments = args
            }
        }

        @JvmStatic
        fun newInstance(illustID: Int, type: String?, tagNames: Array<String>?): FragmentSB {
            val args = Bundle()
            args.putInt(Params.ILLUST_ID, illustID)
            args.putString(Params.DATA_TYPE, type)
            args.putStringArray(Params.TAG_NAMES, tagNames)
            return FragmentSB().apply {
                arguments = args
            }
        }
    }

    override fun initBundle(bundle: Bundle) {
        illustID = bundle.getInt(Params.ILLUST_ID)
        type = bundle.getString(Params.DATA_TYPE, Params.TYPE_ILLUST)
        tagNames = bundle.getStringArray(Params.TAG_NAMES)!!.asList()
    }

    override fun initActivityBundle(bundle: Bundle) {
        lastClass = bundle.getString(Params.LAST_CLASS, "")
    }

    override fun adapter(): BaseAdapter<TagsBean, RecySelectTagBinding> {
        return SAdapter(allItems, mContext)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_select_tag
    }

    override fun repository(): BaseRepo {
        return SelectTagRepo(illustID, type, tagNames)
    }

    private fun submitStar() {
        val tempList = ArrayList<String>()
        for (i in allItems.indices) {
            if (allItems[i].isSelectedLocalOrRemote) {
                tempList.add(allItems[i].name)
            }
        }

        val isPrivate = baseBind.isPrivate.isChecked
        val toastMsg =
            if (isPrivate) {
                getString(R.string.like_novel_success_private)
            } else {
                getString(R.string.like_novel_success_public)
            }

        if (tempList.isEmpty()) {
            if (type == Params.TYPE_ILLUST) {
                Retro.getAppApi()
                    .postLikeIllust(
                        Shaft.sUserModel.access_token,
                        illustID,
                        if (isPrivate) Params.TYPE_PRIVATE else Params.TYPE_PUBLIC,
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : ErrorCtrl<NullResponse>() {
                            override fun next(nullResponse: NullResponse) {
                                Common.showToast(toastMsg)
                                setFollowed()
                            }
                        },
                    )
            } else if (type == Params.TYPE_NOVEL) {
                Retro.getAppApi()
                    .postLikeNovel(
                        Shaft.sUserModel.access_token,
                        illustID,
                        if (isPrivate) Params.TYPE_PRIVATE else Params.TYPE_PUBLIC,
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : ErrorCtrl<NullResponse>() {
                            override fun next(nullResponse: NullResponse) {
                                Common.showToast(toastMsg)
                                setFollowed()
                            }
                        },
                    )
            }
        } else {
            val strings = Array(tempList.size) { "" }
            tempList.toArray(strings)

            if (type == Params.TYPE_ILLUST) {
                Retro.getAppApi()
                    .postLikeIllustWithTags(
                        Shaft.sUserModel.access_token,
                        illustID,
                        if (isPrivate) Params.TYPE_PRIVATE else Params.TYPE_PUBLIC,
                        *strings,
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : ErrorCtrl<NullResponse>() {
                            override fun next(nullResponse: NullResponse) {
                                Common.showToast(toastMsg)
                                setFollowed()
                            }
                        },
                    )
            } else if (type == Params.TYPE_NOVEL) {
                Retro.getAppApi()
                    .postLikeNovelWithTags(
                        Shaft.sUserModel.access_token,
                        illustID,
                        if (isPrivate) Params.TYPE_PRIVATE else Params.TYPE_PUBLIC,
                        *strings,
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : ErrorCtrl<NullResponse>() {
                            override fun next(nullResponse: NullResponse) {
                                Common.showToast(toastMsg)
                                setFollowed()
                            }
                        },
                    )
            }
        }
    }

    private fun setFollowed() {
        val intentString = if (type == Params.TYPE_ILLUST) Params.LIKED_ILLUST else Params.LIKED_NOVEL
        val intent = Intent(intentString)
        intent.putExtra(Params.ID, illustID)
        intent.putExtra(Params.IS_LIKED, true)
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
        mActivity.finish()
    }

    fun addTag(tag: String) {
        var isExist = false
        var i = 0
        while (i < allItems.size) {
            if (allItems[i].name == tag) {
                isExist = true
                break
            }
            i++
        }

        if (isExist) {
            allItems[i].isSelected = true
            mAdapter.notifyItemChanged(i)
            return
        }

        val bookmarkTagsBean = TagsBean()
        bookmarkTagsBean.setCount(0)
        bookmarkTagsBean.isSelected = true
        bookmarkTagsBean.name = tag

        allItems.add(0, bookmarkTagsBean)
        mAdapter.notifyItemInserted(0)
        mRecyclerView.scrollToPosition(0)
        mAdapter.notifyItemRangeChanged(0, allItems.size)
    }

    override fun beforeFirstLoad(tagsBeans: List<TagsBean>) {
        super.beforeFirstLoad(tagsBeans)
        if (Shaft.sSettings.isStarWithTagSelectAll()) {
            for (tagsBean in tagsBeans) {
                if (tagNames.isEmpty() || tagNames.contains(tagsBean.name)) {
                    tagsBean.isSelected = true
                }
            }
        }
    }

    override fun initToolbar(toolbar: Toolbar) {
        super.initToolbar(toolbar)
        toolbar.inflateMenu(R.menu.add_tag)
        toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.action_add) {
                    val builder = QMUIDialog.EditTextDialogBuilder(mActivity)
                    builder
                        .setTitle("添加标签")
                        .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                        .setPlaceholder("请输入标签(收藏夹)名")
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消", object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                dialog.dismiss()
                            }
                        }).addAction("添加", object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                val text = builder.editText.text
                                if (text != null && text.isNotEmpty()) {
                                    addTag(text.toString())
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(mActivity, "请填入标签", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }).show()
                    return true
                }
                return false
            }
        })
    }

    override fun initView() {
        super.initView()
        baseBind.isPrivate.isChecked = Shaft.sSettings.isPrivateStar
        baseBind.submitArea.setOnClickListener { submitStar() }
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.string_238)
    }
}
