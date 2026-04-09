package ceui.lisa.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.effective.android.panel.PanelSwitchHelper
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.UActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.CommentAdapter
import ceui.lisa.adapters.EmojiAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentCommentBinding
import ceui.lisa.databinding.RecyCommentListBinding
import ceui.lisa.helper.BackHandlerHelper
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.FragmentBackHandler
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.EmojiItem
import ceui.lisa.model.ListComment
import ceui.lisa.models.CommentHolder
import ceui.lisa.models.ReplyCommentBean
import ceui.lisa.repo.CommentRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.Emoji
import ceui.lisa.utils.Params
import ceui.lisa.view.EditTextWithSelection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FragmentComment : NetListFragment<FragmentCommentBinding, ListComment, ReplyCommentBean>(), FragmentBackHandler {
    private lateinit var options: Array<String>
    private var workId = 0
    private var dataType: String? = null
    private var title: String? = null
    private var parentCommentID = 0
    private var mHelper: PanelSwitchHelper? = null
    private var selection = 0

    companion object {
        @JvmStatic
        fun newIllustInstance(id: Int, title: String): FragmentComment {
            val args = Bundle()
            args.putInt(Params.ID, id)
            args.putString(Params.DATA_TYPE, Params.TYPE_ILLUST)
            args.putString(Params.ILLUST_TITLE, title)
            val fragment = FragmentComment()
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newNovelInstance(id: Int, title: String): FragmentComment {
            val args = Bundle()
            args.putInt(Params.ID, id)
            args.putString(Params.DATA_TYPE, Params.TYPE_NOVEL)
            args.putString(Params.ILLUST_TITLE, title)
            val fragment = FragmentComment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        workId = bundle.getInt(Params.ID)
        dataType = bundle.getString(Params.DATA_TYPE)
        title = bundle.getString(Params.ILLUST_TITLE)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_comment
    }

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this)
                .contentScrollOutsideEnable(false)
                .logTrack(true)
                .build(false)
        }
    }

    override fun repository(): RemoteRepo<ListComment> {
        return CommentRepo(workId, dataType!!)
    }

    override fun adapter(): BaseAdapter<ReplyCommentBean, RecyCommentListBinding> {
        return CommentAdapter(allItems, mContext).setOnItemClickListener(
            OnItemClickListener { _, position, viewType ->
                if (viewType == 0) {
                    QMUIDialog.MenuDialogBuilder(mActivity)
                        .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                        .addItems(options, DialogInterface.OnClickListener { dialog, which ->
                            if (which == 0) {
                                baseBind.inputBox.hint = getString(R.string.string_176) +
                                    allItems[position].user.name
                                parentCommentID = allItems[position].id
                            } else if (which == 1) {
                                Common.copy(mContext, allItems[position].comment)
                            } else if (which == 2) {
                                val userIntent = Intent(mContext, UActivity::class.java)
                                userIntent.putExtra(Params.USER_ID, allItems[position].user.id)
                                startActivity(userIntent)
                            }
                            dialog.dismiss()
                        })
                        .show()
                } else if (viewType == 1) {
                    val userIntent = Intent(mContext, UActivity::class.java)
                    userIntent.putExtra(Params.USER_ID, allItems[position].user.id)
                    startActivity(userIntent)
                } else if (viewType == 2) {
                    QMUIDialog.MenuDialogBuilder(mActivity)
                        .setSkinManager(QMUISkinManager.defaultInstance(mActivity))
                        .addItems(options, DialogInterface.OnClickListener { dialog, which ->
                            if (which == 0) {
                                baseBind.inputBox.hint =
                                    getString(R.string.string_176) +
                                    allItems[position].parent_comment.user.name
                                parentCommentID = allItems[position].parent_comment.id
                            } else if (which == 1) {
                                Common.copy(mContext, allItems[position].parent_comment.comment)
                            } else if (which == 2) {
                                val userIntent = Intent(mContext, UActivity::class.java)
                                userIntent.putExtra(Params.USER_ID, allItems[position].parent_comment.user.id)
                                startActivity(userIntent)
                            }
                            dialog.dismiss()
                        })
                        .show()
                } else if (viewType == 3) {
                    val userIntent = Intent(mContext, UActivity::class.java)
                    userIntent.putExtra(Params.USER_ID, allItems[position].parent_comment.user.id)
                    startActivity(userIntent)
                }
            },
        )
    }

    override fun getToolbarTitle(): String {
        return title + getString(R.string.string_175)
    }

    override fun initRecyclerView() {
        baseBind.recyclerView.layoutManager = LinearLayoutManager(mContext)
    }

    override fun beforeFirstLoad(items: List<ReplyCommentBean>) {
        for (replyCommentBean in items) {
            val comment = replyCommentBean.comment
            if (Emoji.hasEmoji(comment)) {
                val newComment = Emoji.transform(comment)
                replyCommentBean.setCommentWithConvertedEmoji(newComment)
            }

            if (replyCommentBean.parent_comment != null) {
                val parentComment = replyCommentBean.parent_comment.comment
                if (Emoji.hasEmoji(parentComment)) {
                    val newComment = Emoji.transform(parentComment)
                    replyCommentBean.parent_comment.setCommentWithConvertedEmoji(newComment)
                }
            }
        }
    }

    override fun beforeNextLoad(items: List<ReplyCommentBean>) {
        for (item in items) {
            val comment = item.comment
            if (Emoji.hasEmoji(comment)) {
                val newComment = Emoji.transform(comment)
                item.setCommentWithConvertedEmoji(newComment)
            }

            if (item.parent_comment != null) {
                val parentComment = item.parent_comment.comment
                if (Emoji.hasEmoji(parentComment)) {
                    val newComment = Emoji.transform(parentComment)
                    item.parent_comment.setCommentWithConvertedEmoji(newComment)
                }
            }
        }
    }

    override fun initView() {
        super.initView()
        options = arrayOf(
            getString(R.string.string_172),
            getString(R.string.string_173),
            getString(R.string.string_174),
        )
        baseBind.post.setOnClickListener {
            if (!Shaft.sUserModel.user.isIs_mail_authorized) {
                val builder = AlertDialog.Builder(mContext)
                builder.setMessage(R.string.string_158)
                builder.setPositiveButton(R.string.string_159) { dialog, which ->
                    TemplateActivity.startBindEmail(mContext)
                }
                builder.setNegativeButton(R.string.string_160, null)
                val alertDialog = builder.create()
                alertDialog.show()
                alertDialog
                    .getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(androidx.appcompat.R.attr.colorPrimary)
                alertDialog
                    .getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(androidx.appcompat.R.attr.colorPrimary)
                return@setOnClickListener
            }

            if (baseBind.inputBox.text.toString().isEmpty()) {
                Common.showToast(getString(R.string.string_161), 3)
                return@setOnClickListener
            }

            val nullCtrl = object : NullCtrl<CommentHolder>() {
                override fun onSubscribe(d: Disposable) {
                    Common.hideKeyboard(mActivity)
                    mHelper?.resetState()
                    baseBind.inputBox.setHint(R.string.string_162)
                    baseBind.inputBox.setText("")
                    baseBind.progress.visibility = View.VISIBLE
                }

                override fun success(commentHolder: CommentHolder) {
                    if (allItems.size == 0) {
                        mRecyclerView.visibility = View.VISIBLE
                        emptyRela.visibility = View.INVISIBLE
                    }

                    val replyCommentBean = commentHolder.comment
                    if (Emoji.hasEmoji(replyCommentBean.comment)) {
                        replyCommentBean.setCommentWithConvertedEmoji(
                            Emoji.transform(replyCommentBean.comment),
                        )
                        allItems.add(0, replyCommentBean)
                    } else {
                        allItems.add(0, replyCommentBean)
                    }
                    mAdapter.notifyItemInserted(0)
                    baseBind.recyclerView.scrollToPosition(0)
                }

                override fun must(isSuccess: Boolean) {
                    baseBind.progress.visibility = View.GONE
                }
            }
            getCommentHolder()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nullCtrl)
        }
        baseBind.clear.setOnClickListener {
            if (baseBind.inputBox.text.toString().isNotEmpty()) {
                baseBind.inputBox.setText("")
                return@setOnClickListener
            }
            if (parentCommentID != 0) {
                baseBind.inputBox.setHint(R.string.string_163)
                parentCommentID = 0
            }
        }

        val recyclerView: RecyclerView = baseBind.root.findViewById(R.id.recy_list)
        val layoutManager = GridLayoutManager(context, 6)
        recyclerView.layoutManager = layoutManager
        val adapter = EmojiAdapter(Emoji.getEmojis(), context!!)
        adapter.setOnItemClickListener(
            OnItemClickListener { _, position, _ ->
                val item: EmojiItem? = adapter.getItemAt(position)
                val name = item!!.name
                val show = baseBind.inputBox.text.toString()
                if (selection < show.length) {
                    val left = show.substring(0, selection)
                    val right = show.substring(selection)

                    baseBind.inputBox.setText(String.format("%s%s%s", left, name, right))
                    baseBind.inputBox.setSelection(selection + name.length)
                } else {
                    val result = show + name

                    baseBind.inputBox.setText(result)
                    baseBind.inputBox.setSelection(result.length)
                }
                Common.showLog(className + selection)
            },
        )
        recyclerView.adapter = adapter
        baseBind.inputBox.setOnSelectionChange(
            object : EditTextWithSelection.OnSelectionChange {
                override fun onChange(start: Int, end: Int) {
                    if (start != 0) {
                        selection = start
                    }
                }
            },
        )
    }

    override fun onBackPressed(): Boolean {
        val childResult = BackHandlerHelper.handleBackPress(this)
        return childResult || mHelper!!.hookSystemBackByPanelSwitcher()
    }

    private fun getCommentHolder(): Observable<CommentHolder> {
        return if (dataType === Params.TYPE_ILLUST) {
            if (parentCommentID != 0) {
                Retro.getAppApi().postIllustComment(
                    Shaft.sUserModel.access_token,
                    workId,
                    baseBind.inputBox.text.toString(),
                    parentCommentID,
                )
            } else {
                Retro.getAppApi().postIllustComment(
                    Shaft.sUserModel.access_token,
                    workId,
                    baseBind.inputBox.text.toString(),
                )
            }
        } else {
            if (parentCommentID != 0) {
                Retro.getAppApi().postNovelComment(
                    Shaft.sUserModel.access_token,
                    workId,
                    baseBind.inputBox.text.toString(),
                    parentCommentID,
                )
            } else {
                Retro.getAppApi().postNovelComment(
                    Shaft.sUserModel.access_token,
                    workId,
                    baseBind.inputBox.text.toString(),
                )
            }
        }
    }
}
