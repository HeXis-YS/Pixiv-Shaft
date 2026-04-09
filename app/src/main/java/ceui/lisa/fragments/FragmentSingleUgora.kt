package ceui.lisa.fragments

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.scwang.smart.refresh.header.FalsifyFooter
import com.scwang.smart.refresh.header.FalsifyHeader
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import ceui.lisa.R
import ceui.lisa.activities.SearchActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.UActivity
import ceui.lisa.cache.Cache
import ceui.lisa.core.DownloadItem
import ceui.lisa.core.Manager
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.databinding.FragmentUgoraBinding
import ceui.lisa.dialogs.MuteDialog
import ceui.lisa.download.IllustDownload
import ceui.lisa.file.LegacyFile
import ceui.lisa.file.OutPut
import ceui.lisa.http.ErrorCtrl
import ceui.lisa.interfaces.Callback
import ceui.lisa.models.GifResponse
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.TagsBean
import ceui.lisa.notification.BaseReceiver
import ceui.lisa.notification.CallBackReceiver
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import ceui.lisa.utils.SearchTypeUtil.Companion.SEARCH_TYPE_DB_KEYWORD
import ceui.lisa.utils.ShareIllust
import ceui.lisa.viewmodel.AppLevelViewModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rxhttp.wrapper.entity.Progress

/**
 * 插画详情
 */
class FragmentSingleUgora : BaseFragment<FragmentUgoraBinding>() {

    private var illust: IllustsBean? = null
    private var receiver: CallBackReceiver? = null
    private var playReceiver: CallBackReceiver? = null

    companion object {
        @JvmStatic
        fun newInstance(illust: IllustsBean): FragmentSingleUgora {
            val args = Bundle()
            args.putSerializable(Params.CONTENT, illust)
            val fragment = FragmentSingleUgora()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        illust = bundle.getSerializable(Params.CONTENT) as IllustsBean
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_ugora
    }

    private fun loadImage() {
        val targetIllust = illust ?: return
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                Glide.with(mContext)
                    .load(GlideUtil.getSquare(targetIllust))
                    .apply(com.bumptech.glide.request.RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(baseBind.bgImage)
            }

            Configuration.UI_MODE_NIGHT_YES -> {
                baseBind.bgImage.setImageResource(R.color.black_to_grey)
            }
        }

        val imageSize = (
            mContext.resources.displayMetrics.widthPixels -
                2 * mContext.resources.getDimensionPixelSize(R.dimen.twelve_dp)
            )
        val params = baseBind.illustImage.layoutParams
        params.height = imageSize * targetIllust.height / targetIllust.width
        params.width = imageSize
        baseBind.illustImage.layoutParams = params

        Glide.with(mContext)
            .asDrawable()
            .load(GlideUtil.getLargeImage(targetIllust))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(
                object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        baseBind.illustImage.setImageDrawable(resource)
                    }
                },
            )
    }

    override fun initData() {
        val targetIllust = illust ?: return
        loadImage()

        receiver = CallBackReceiver(BaseReceiver.CallBack { _, intent ->
            val bundle = intent.extras
            if (bundle != null) {
                val id = bundle.getInt(Params.ID)
                if (targetIllust.id == id) {
                    val isLiked = bundle.getBoolean(Params.IS_LIKED)
                    if (isLiked) {
                        targetIllust.isIs_bookmarked = true
                        baseBind.postLike.setImageResource(R.drawable.ic_favorite_red_24dp)
                    } else {
                        targetIllust.isIs_bookmarked = false
                        baseBind.postLike.setImageResource(R.drawable.ic_favorite_black_24dp)
                    }
                }
            }
        })
        IntentFilter().apply {
            addAction(Params.LIKED_ILLUST)
            LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver!!, this)
        }

        playReceiver = CallBackReceiver(BaseReceiver.CallBack { _, intent ->
            baseBind.progressLayout.donutProgress.visibility = View.GONE
            val bundle = intent.extras
            if (bundle != null) {
                val id = bundle.getInt(Params.ID)
                if (targetIllust.id == id) {
                    nowPlayGif()
                }
            }
        })
        IntentFilter().apply {
            addAction(Params.PLAY_GIF)
            LocalBroadcastManager.getInstance(mContext).registerReceiver(playReceiver!!, this)
        }

        PixivOperate.setBack(targetIllust.id) { progress ->
            baseBind.progressLayout.donutProgress.progress =
                Math.round(progress * 100).toFloat()
        }
    }

    override fun onDestroy() {
        try {
            receiver?.let { LocalBroadcastManager.getInstance(mContext).unregisterReceiver(it) }
            playReceiver?.let { LocalBroadcastManager.getInstance(mContext).unregisterReceiver(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    fun nowPlayGif() {
        val targetIllust = illust ?: return
        val gifFile = LegacyFile.gifResultFile(mContext, targetIllust)
        PixivOperate.setBack(targetIllust.id) { progress ->
            baseBind.progressLayout.donutProgress.progress =
                Math.round(progress * 100).toFloat()
        }
        Common.showLog("nowPlayGif " + gifFile.path)
        if (gifFile.exists() && gifFile.length() > 1024) {
            Common.showLog("GIF文件已存在，直接播放")
            baseBind.playGif.visibility = View.INVISIBLE
            baseBind.progressLayout.donutProgress.visibility = View.INVISIBLE
            Glide.with(mContext)
                .asGif()
                .load(gifFile)
                .placeholder(baseBind.illustImage.drawable)
                .into(baseBind.illustImage)
        } else {
            val hasDownload = Shaft.getMMKV().decodeBool(Params.ILLUST_ID + "_" + targetIllust.id)
            val zipFile = LegacyFile.gifZipFile(mContext, targetIllust)
            if (hasDownload && zipFile.exists() && zipFile.length() > 1024) {
                baseBind.playGif.visibility = View.INVISIBLE
                baseBind.progressLayout.donutProgress.visibility = View.VISIBLE
                PixivOperate.unzipAndPlay(mContext, targetIllust)
            } else {
                Common.showToast("获取GIF信息")
                baseBind.progress.visibility = View.VISIBLE
                PixivOperate.getGifInfo(targetIllust, object : ErrorCtrl<GifResponse>() {
                    override fun next(gifResponse: GifResponse) {
                        baseBind.progress.visibility = View.INVISIBLE
                        Cache.get().saveModel(Params.ILLUST_ID + "_" + targetIllust.id, gifResponse)
                        Common.showToast("下载GIF文件")
                        val downloadItem: DownloadItem =
                            IllustDownload.downloadGif(gifResponse, targetIllust)
                        Manager.get().setCallback(downloadItem.uuid, object : Callback<Progress> {
                            override fun doSomething(t: Progress) {
                                try {
                                    if (targetIllust.id == Manager.get().currentIllustID) {
                                        baseBind.playGif.visibility = View.INVISIBLE
                                        baseBind.progressLayout.donutProgress.visibility =
                                            View.VISIBLE
                                        baseBind.progressLayout.donutProgress.progress =
                                            t.progress.toFloat()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    }
                })
            }
        }
    }

    override fun initView() {
        val targetIllust = illust ?: return

        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }

        if (targetIllust.id == 0) {
            baseBind.toolbar.setTitle(R.string.string_206)
            baseBind.refreshLayout.visibility = View.INVISIBLE
            return
        }

        if (targetIllust.id == Manager.get().currentIllustID) {
            Manager.get().setCallback(object : Callback<Progress> {
                override fun doSomething(t: Progress) {
                    try {
                        if (targetIllust.id == Manager.get().currentIllustID) {
                            baseBind.playGif.visibility = View.INVISIBLE
                            baseBind.progressLayout.donutProgress.visibility = View.VISIBLE
                            baseBind.progressLayout.donutProgress.progress = t.progress.toFloat()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }

        baseBind.playGif.setOnClickListener { nowPlayGif() }

        baseBind.refreshLayout.visibility = View.VISIBLE
        baseBind.refreshLayout.setEnableLoadMore(true)
        baseBind.refreshLayout.setRefreshHeader(FalsifyHeader(mContext))
        baseBind.refreshLayout.setRefreshFooter(FalsifyFooter(mContext))
        baseBind.title.text = targetIllust.title
        baseBind.title.setOnLongClickListener {
            Common.copy(mContext, targetIllust.title)
            true
        }

        baseBind.toolbar.inflateMenu(R.menu.share)
        baseBind.toolbar.menu.findItem(R.id.action_show_original).isVisible = false
        baseBind.toolbar.setOnMenuItemClickListener(
            Toolbar.OnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_share -> {
                        object : ShareIllust(mContext, targetIllust) {
                            override fun onPrepare() {
                            }
                        }.execute()
                        true
                    }

                    R.id.action_dislike -> {
                        val muteDialog = MuteDialog.newInstance(targetIllust)
                        muteDialog.show(childFragmentManager, "MuteDialog")
                        true
                    }

                    R.id.action_copy_link -> {
                        val url = ShareIllust.URL_Head + targetIllust.id
                        Common.copy(mContext, url)
                        true
                    }

                    R.id.action_show_original -> true

                    R.id.action_mute_illust -> {
                        PixivOperate.muteIllust(targetIllust)
                        true
                    }

                    else -> false
                }
            },
        )

        baseBind.download.setOnClickListener {
            val gifFile = LegacyFile.gifResultFile(mContext, targetIllust)
            if (gifFile.exists() && gifFile.length() > 1024) {
                OutPut.outPutGif(mContext, gifFile, targetIllust)
                if (Shaft.sSettings.isAutoPostLikeWhenDownload && !targetIllust.isIs_bookmarked) {
                    PixivOperate.postLikeDefaultStarType(targetIllust)
                }
            } else {
                IllustDownload.downloadGif(targetIllust)
                Common.showToast("1" + requireContext().getString(R.string.has_been_added))
            }
        }
        baseBind.userName.setOnLongClickListener {
            Common.copy(mContext, targetIllust.user.name.toString())
            true
        }
        baseBind.related.setOnClickListener {
            TemplateActivity.startRelatedIllust(mContext, targetIllust.id, targetIllust.title)
        }
        baseBind.comment.setOnClickListener {
            TemplateActivity.startIllustComments(mContext, targetIllust.id, targetIllust.title)
        }
        baseBind.illustLike.setOnClickListener {
            val intent = Intent(mContext, TemplateActivity::class.java)
            intent.putExtra(Params.CONTENT, targetIllust)
            intent.putExtra(
                TemplateActivity.EXTRA_FRAGMENT,
                TemplateActivity.FRAGMENT_LIKE_USER_LIST,
            )
            startActivity(intent)
        }
        if (targetIllust.isIs_bookmarked) {
            baseBind.postLike.setImageResource(R.drawable.ic_favorite_red_24dp)
        } else {
            baseBind.postLike.setImageResource(R.drawable.ic_favorite_black_24dp)
        }
        baseBind.postLike.setOnClickListener {
            if (targetIllust.isIs_bookmarked) {
                baseBind.postLike.setImageResource(R.drawable.ic_favorite_black_24dp)
            } else {
                baseBind.postLike.setImageResource(R.drawable.ic_favorite_red_24dp)
            }
            PixivOperate.postLikeDefaultStarType(targetIllust)
        }
        baseBind.postLike.setOnLongClickListener {
            TemplateActivity.startTagStar(
                mContext,
                targetIllust.id,
                Params.TYPE_ILLUST,
                targetIllust.tagNames,
                javaClass.simpleName,
            )
            true
        }
        val seeUser = View.OnClickListener {
            val intent = Intent(mContext, UActivity::class.java)
            intent.putExtra(Params.USER_ID, targetIllust.user.id)
            startActivity(intent)
        }
        baseBind.userHead.setOnClickListener(seeUser)
        baseBind.userName.setOnClickListener(seeUser)

        baseBind.follow.setOnClickListener {
            val integerValue = Shaft.appViewModel.getFollowUserLiveData(targetIllust.user.id).value
            if (AppLevelViewModel.FollowUserStatus.isFollowed(integerValue ?: AppLevelViewModel.FollowUserStatus.UNKNOWN)) {
                PixivOperate.postUnFollowUser(targetIllust.user.id)
                targetIllust.user.setIs_followed(false)
            } else {
                PixivOperate.postFollowUser(targetIllust.user.id, Params.TYPE_PUBLIC)
                targetIllust.user.setIs_followed(true)
            }
        }

        baseBind.follow.setOnLongClickListener {
            val integerValue = Shaft.appViewModel.getFollowUserLiveData(targetIllust.user.id).value
            if (!AppLevelViewModel.FollowUserStatus.isFollowed(integerValue ?: AppLevelViewModel.FollowUserStatus.UNKNOWN)) {
                targetIllust.user.setIs_followed(true)
            }
            PixivOperate.postFollowUser(targetIllust.user.id, Params.TYPE_PRIVATE)
            true
        }

        Glide.with(mContext)
            .load(GlideUtil.getUrl(targetIllust.user.profile_image_urls.medium))
            .into(baseBind.userHead)

        baseBind.userName.text = targetIllust.user.name

        val sizeString = SpannableString(
            getString(R.string.string_193, targetIllust.width, targetIllust.height),
        )
        val currentPrimaryColorId =
            Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary)
        sizeString.setSpan(
            ForegroundColorSpan(currentPrimaryColorId),
            sizeString.length - targetIllust.size.length,
            sizeString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        baseBind.illustPx.text = sizeString

        baseBind.illustTag.adapter = object : TagAdapter<TagsBean>(targetIllust.tags) {
            override fun getView(parent: FlowLayout, position: Int, s: TagsBean): View {
                val tv = LayoutInflater.from(mContext).inflate(
                    R.layout.recy_single_line_text_new,
                    parent,
                    false,
                ) as TextView
                var tag = s.name
                if (!TextUtils.isEmpty(s.translated_name)) {
                    tag += "/" + s.translated_name
                }
                tv.text = tag
                return tv
            }
        }
        baseBind.illustTag.setOnTagClickListener(
            object : TagFlowLayout.OnTagClickListener {
                override fun onTagClick(view: View, position: Int, parent: FlowLayout): Boolean {
                    val intent = Intent(mContext, SearchActivity::class.java)
                    intent.putExtra(Params.KEY_WORD, targetIllust.tags[position].name)
                    intent.putExtra(Params.INDEX, 0)
                    startActivity(intent)
                    return true
                }
            },
        )
        baseBind.illustTag.setOnTagLongClickListener { _, position, _ ->
            val tagName = targetIllust.tags[position].name
            viewLifecycleOwner.lifecycleScope.launch {
                val isPinned = withContext(Dispatchers.IO) {
                    PixivOperate.getSearchHistory(tagName, SEARCH_TYPE_DB_KEYWORD)?.isPinned == true
                }
                QMUIDialog.MessageDialogBuilder(mContext)
                    .setTitle(tagName)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addAction(
                        if (isPinned) getString(R.string.string_443) else getString(R.string.string_442),
                    ) { dialog, _ ->
                        PixivOperate.insertPinnedSearchHistory(
                            tagName,
                            SEARCH_TYPE_DB_KEYWORD,
                            !isPinned,
                        )
                        Common.showToast(R.string.operate_success)
                        dialog.dismiss()
                    }
                    .addAction(getString(R.string.string_120)) { dialog, _ ->
                        Common.copy(mContext, tagName)
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            true
        }

        if (!TextUtils.isEmpty(targetIllust.caption)) {
            baseBind.description.visibility = View.VISIBLE
            baseBind.description.setHtml(targetIllust.caption)
        } else {
            baseBind.description.visibility = View.GONE
        }
        baseBind.illustDate.text = Common.getLocalYYYYMMDDHHMMString(targetIllust.create_date)
        baseBind.illustView.text = targetIllust.total_view.toString()
        baseBind.illustLike.text = targetIllust.total_bookmarks.toString()

        val userString = SpannableString(
            getString(R.string.string_195, targetIllust.user.id),
        )
        userString.setSpan(
            ForegroundColorSpan(currentPrimaryColorId),
            userString.length - targetIllust.user.id.toString().length,
            userString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        baseBind.userId.text = userString
        baseBind.userId.setOnClickListener {
            Common.copy(mContext, targetIllust.user.id.toString())
        }
        val illustString = SpannableString(
            getString(R.string.string_194, targetIllust.id),
        )
        illustString.setSpan(
            ForegroundColorSpan(currentPrimaryColorId),
            illustString.length - targetIllust.id.toString().length,
            illustString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        baseBind.illustId.text = illustString
        baseBind.illustId.setOnClickListener {
            Common.copy(mContext, targetIllust.id.toString())
        }

        Shaft.appViewModel.getFollowUserLiveData(targetIllust.user.id).observe(
            this,
            Observer { integer ->
                if (integer != null) {
                    updateFollowUserUI(integer)
                }
            },
        )
    }

    override fun vertical() {
        val headParams = baseBind.head.layoutParams
        headParams.height = Shaft.statusHeight + Shaft.toolbarHeight
        baseBind.head.layoutParams = headParams
        baseBind.toolbar.setPadding(0, Shaft.statusHeight, 0, 0)
    }

    override fun horizon() {
        val headParams = baseBind.head.layoutParams
        headParams.height = Shaft.statusHeight * 3 / 5 + Shaft.toolbarHeight
        baseBind.head.layoutParams = headParams
    }

    private fun updateFollowUserUI(status: Int) {
        if (AppLevelViewModel.FollowUserStatus.isFollowed(status)) {
            baseBind.follow.setText(R.string.string_177)
        } else {
            baseBind.follow.setText(R.string.string_4)
        }
    }
}
