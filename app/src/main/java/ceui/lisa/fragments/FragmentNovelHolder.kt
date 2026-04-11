package ceui.lisa.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import ceui.lisa.R
import ceui.lisa.activities.BaseActivity
import ceui.lisa.activities.NovelActivity
import ceui.lisa.activities.SearchActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.adapters.VAdapter
import ceui.lisa.adapters.VNewAdapter
import ceui.lisa.cache.Cache
import ceui.lisa.database.DownloadEntity
import ceui.lisa.databinding.FragmentNovelHolderBinding
import ceui.lisa.download.IllustDownload
import ceui.lisa.helper.NovelParseHelper
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.Callback
import ceui.lisa.models.NovelBean
import ceui.lisa.models.NovelDetail
import ceui.lisa.models.NovelSearchResponse
import ceui.lisa.models.TagsBean
import ceui.lisa.models.WebNovel
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import ceui.lisa.view.ScrollChange
import ceui.loxia.SpaceHolder
import ceui.loxia.TextDescHolder
import ceui.loxia.novel.NovelImageHolder
import ceui.loxia.novel.NovelTextHolder
import ceui.refactor.CommonAdapter
import ceui.refactor.ListItemHolder
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.PathUtils
import com.bumptech.glide.Glide
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.skydoves.transformationlayout.OnTransformFinishListener
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import gdut.bsx.share2.Share2
import gdut.bsx.share2.ShareContentType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

class FragmentNovelHolder : BaseFragment<FragmentNovelHolderBinding>() {
    private var isOpen = false
    private var mNovelBean: NovelBean? = null
    private var mNovelDetail: NovelDetail? = null
    private var mWebNovel: WebNovel? = null

    companion object {
        @JvmStatic
        fun newInstance(novelBean: NovelBean?): FragmentNovelHolder {
            val args = Bundle()
            args.putSerializable(Params.CONTENT, novelBean)
            val fragment = FragmentNovelHolder()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_novel_holder
    }

    override fun initBundle(bundle: Bundle) {
        mNovelBean = bundle.getSerializable(Params.CONTENT) as NovelBean?
    }

    override fun initView() {
        BarUtils.setNavBarColor(mActivity, resources.getColor(R.color.hito_bg))
        if (Shaft.sSettings.getNovelHolderColor() != 0) {
            setBackgroundColor(Shaft.sSettings.getNovelHolderColor())
        }
        baseBind.fab.setOnClickListener {
            baseBind.transformationLayout.startTransform()
        }
        baseBind.transformationLayout.onTransformFinishListener =
            object : OnTransformFinishListener {
                override fun onFinish(isTransformed: Boolean) {
                    Common.showLog(className + isTransformed)
                    isOpen = isTransformed
                }
            }
    }

    override fun initData() {
        displayNovel(mNovelBean!!)
    }

    fun setBackgroundColor(color: Int) {
        Common.showLog(className + color)
        baseBind.relaRoot.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        Common.showLog(className + color)
        baseBind.toolbar.overflowIcon!!.setTint(Common.getNovelTextColor())
        setNovelAdapter()
    }

    private fun displayNovel(novelBean: NovelBean) {
        mNovelBean = novelBean
        if (mNovelBean!!.isIs_bookmarked()) {
            baseBind.like.text = mContext.getString(R.string.string_179)
        } else {
            baseBind.like.text = mContext.getString(R.string.string_180)
        }
        Common.showLog(className + "getNovel 000")
        baseBind.like.setOnClickListener {
            Common.showLog(className + "getNovel 111")
            PixivOperate.postLikeNovel(mNovelBean, Shaft.sUserModel, Params.TYPE_PUBLIC, baseBind.like)
        }
        baseBind.like.setOnLongClickListener {
            TemplateActivity.startTagStar(mContext, mNovelBean!!.getId(), Params.TYPE_NOVEL, mNovelBean!!.getTagNames())
            true
        }

        val seeUser =
            View.OnClickListener {
                Common.showUser(mContext, mNovelBean!!.getUser())
            }
        baseBind.userHead.setOnClickListener(seeUser)
        baseBind.userName.setOnClickListener(seeUser)
        baseBind.userName.text = mNovelBean!!.getUser().getName()
        baseBind.viewPager.layoutManager = ScrollChange(mContext)
        baseBind.viewPager.setHasFixedSize(false)
        baseBind.novelTitle.text =
            String.format("%s%s", getString(R.string.string_182), mNovelBean!!.getTitle())
        if (mNovelBean!!.getSeries() != null && !TextUtils.isEmpty(mNovelBean!!.getSeries().title)) {
            baseBind.novelSeries.visibility = View.VISIBLE
            baseBind.novelSeries.text =
                String.format("%s%s", getString(R.string.string_183), mNovelBean!!.getSeries().title)
            baseBind.novelSeries.setOnClickListener {
                TemplateActivity.startNovelSeriesDetail(mContext, mNovelBean!!.getSeries().id)
            }
        } else {
            baseBind.novelSeries.visibility = View.GONE
        }
        if (mNovelBean!!.getTags() != null && mNovelBean!!.getTags().isNotEmpty()) {
            baseBind.hotTags.adapter =
                object : TagAdapter<TagsBean>(mNovelBean!!.getTags()) {
                    override fun getView(parent: FlowLayout, position: Int, trendTagsBean: TagsBean): View {
                        val textView =
                            LayoutInflater.from(mContext).inflate(
                                R.layout.recy_single_novel_tag_text_small,
                                parent,
                                false,
                            ) as TextView
                        textView.text = trendTagsBean.getName()
                        return textView
                    }
                }
            baseBind.hotTags.setOnTagClickListener(
                TagFlowLayout.OnTagClickListener { _, position, _ ->
                    val intent = Intent(mContext, SearchActivity::class.java)
                    intent.putExtra(Params.KEY_WORD, mNovelBean!!.getTags()[position].getName())
                    intent.putExtra(Params.INDEX, 1)
                    startActivity(intent)
                    false
                },
            )
        }
        if (TextUtils.isEmpty(mNovelBean!!.getCaption())) {
            baseBind.description.visibility = View.GONE
        } else {
            baseBind.description.visibility = View.VISIBLE
            baseBind.description.setHtml(mNovelBean!!.getCaption())
        }
        baseBind.publishTime.text = Common.getLocalYYYYMMDDHHMMString(mNovelBean!!.getCreate_date())
        baseBind.viewCount.text = mNovelBean!!.getTotal_view().toString()
        baseBind.bookmarkCount.text = mNovelBean!!.getTotal_bookmarks().toString()
        baseBind.comment.setOnClickListener {
            TemplateActivity.startNovelComments(mContext, mNovelBean!!.getId(), mNovelBean!!.getTitle())
        }
        Glide.with(mContext).load(GlideUtil.getHead(mNovelBean!!.getUser())).into(baseBind.userHead)

        PixivOperate.insertNovelViewHistory(novelBean)
        baseBind.viewPager.visibility = View.INVISIBLE
        if (novelBean.isLocalSaved()) {
            baseBind.progressRela.visibility = View.INVISIBLE
            mNovelDetail = Cache.get().getModel(Params.NOVEL_KEY + mNovelBean!!.getId(), NovelDetail::class.java)
            refreshDetail(mNovelDetail!!)
        } else {
            baseBind.progressRela.visibility = View.VISIBLE
            Retro.getAppApi().getNovelDetailV2(Shaft.sUserModel.getAccess_token(), novelBean.getId().toLong())
                .enqueue(
                    object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>,
                        ) {
                            baseBind.progressRela.visibility = View.INVISIBLE
                            object : WebNovelParser(response) {
                                override fun onNovelPrepared(
                                    novelDetail: NovelDetail,
                                    webNovel: WebNovel,
                                ) {
                                    mWebNovel = webNovel
                                    novelDetail.setParsedChapters(
                                        NovelParseHelper.tryParseChapters(novelDetail.getNovel_text()),
                                    )
                                    refreshDetail(novelDetail)
                                }
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            baseBind.progressRela.visibility = View.INVISIBLE
                        }
                    },
                )
        }

        baseBind.toolbar.setOnTouchListener(
            object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    return baseBind.awesomeCardCon.dispatchTouchEvent(event)
                }
            },
        )
    }

    private fun refreshDetail(novelDetail: NovelDetail) {
        mNovelDetail = novelDetail
        baseBind.viewPager.visibility = View.VISIBLE
        baseBind.awesomeCardCon.setOnTouchListener(
            object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (isOpen) {
                        baseBind.transformationLayout.finishTransform()
                        isOpen = false
                        return true
                    }
                    return baseBind.viewPager.dispatchTouchEvent(event)
                }
            },
        )

        setNovelAdapter()

        if (novelDetail.getSeries_prev() != null && novelDetail.getSeries_prev().getId() != 0) {
            baseBind.showPrev.visibility = View.VISIBLE
            baseBind.showPrev.setOnClickListener {
                baseBind.transformationLayout.finishTransform()
                Retro.getAppApi().getNovelByID(Shaft.sUserModel.getAccess_token(), novelDetail.getSeries_prev().getId().toLong())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : NullCtrl<NovelSearchResponse>() {
                            override fun success(novelSearchResponse: NovelSearchResponse) {
                                displayNovel(novelSearchResponse.getNovel())
                            }
                        },
                    )
            }
        } else {
            baseBind.showPrev.visibility = View.INVISIBLE
        }
        if (novelDetail.getSeries_next() != null && novelDetail.getSeries_next().getId() != 0) {
            baseBind.showNext.visibility = View.VISIBLE
            baseBind.showNext.setOnClickListener {
                baseBind.transformationLayout.finishTransform()
                Retro.getAppApi().getNovelByID(Shaft.sUserModel.getAccess_token(), novelDetail.getSeries_next().getId().toLong())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        object : NullCtrl<NovelSearchResponse>() {
                            override fun success(novelSearchResponse: NovelSearchResponse) {
                                displayNovel(novelSearchResponse.getNovel())
                            }
                        },
                    )
            }
        } else {
            baseBind.showNext.visibility = View.INVISIBLE
        }
        baseBind.toolbar.menu.clear()
        baseBind.toolbar.inflateMenu(R.menu.novel_read_menu)
        baseBind.toolbar.overflowIcon!!.setTint(Common.getNovelTextColor())
        baseBind.saveNovelTxt.setOnClickListener {
            IllustDownload.downloadNovel(
                mContext as BaseActivity<*>,
                mNovelBean!!,
                novelDetail,
                object : Callback<Uri> {
                    override fun doSomething(t: Uri) {
                        Common.showToast(getString(R.string.string_279), 2)
                    }
                },
            )
        }
        baseBind.toolbar.setOnMenuItemClickListener(
            object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    if (item.itemId == R.id.action_change_color) {
                        if (Shaft.sSettings.getNovelHolderColor() != 0) {
                            ColorPickerDialog.newBuilder()
                                .setDialogId(Params.DIALOG_NOVEL_BG_COLOR)
                                .setColor(Shaft.sSettings.getNovelHolderColor())
                                .show(mActivity)
                        } else {
                            ColorPickerDialog.newBuilder()
                                .setDialogId(Params.DIALOG_NOVEL_BG_COLOR)
                                .setColor(resources.getColor(R.color.novel_holder))
                                .show(mActivity)
                        }
                        return true
                    } else if (item.itemId == R.id.action_change_text_color) {
                        if (Shaft.sSettings.getNovelHolderTextColor() != 0) {
                            ColorPickerDialog.newBuilder()
                                .setDialogId(Params.DIALOG_NOVEL_TEXT_COLOR)
                                .setColor(Shaft.sSettings.getNovelHolderTextColor())
                                .show(mActivity)
                        } else {
                            ColorPickerDialog.newBuilder()
                                .setDialogId(Params.DIALOG_NOVEL_TEXT_COLOR)
                                .setColor(resources.getColor(R.color.white))
                                .show(mActivity)
                        }
                        return true
                    } else if (item.itemId == R.id.action_save) {
                        mNovelBean!!.setLocalSaved(true)
                        val fileName = Params.NOVEL_KEY + mNovelBean!!.getId()
                        Cache.get().saveModel(fileName, mNovelDetail!!)
                        val downloadEntity = DownloadEntity()
                        downloadEntity.fileName = fileName
                        downloadEntity.downloadTime = System.currentTimeMillis()
                        downloadEntity.filePath = PathUtils.getInternalAppCachePath()
                        downloadEntity.illustGson = Shaft.sGson.toJson(mNovelBean)
                        PixivOperate.insertDownload(downloadEntity)
                        Common.showToast(getString(R.string.string_181), 2)
                        baseBind.transformationLayout.finishTransform()
                        return true
                    } else if (item.itemId == R.id.action_txt) {
                        IllustDownload.downloadNovel(
                            mContext as BaseActivity<*>,
                            mNovelBean!!,
                            novelDetail,
                            object : Callback<Uri> {
                                override fun doSomething(t: Uri) {
                                    Common.showToast(getString(R.string.string_279), 2)
                                }
                            },
                        )
                        return true
                    } else if (item.itemId == R.id.action_txt_and_share) {
                        IllustDownload.downloadNovel(
                            mActivity as BaseActivity<*>,
                            mNovelBean!!,
                            novelDetail,
                            object : Callback<Uri> {
                                override fun doSomething(uri: Uri) {
                                    Share2.Builder(mActivity)
                                        .setContentType(ShareContentType.FILE)
                                        .setShareFileUri(uri)
                                        .setTitle("Share File")
                                        .build()
                                        .shareBySystem()
                                }
                            },
                        )
                        Common.showToast(getString(R.string.string_279), 2)
                        return true
                    }
                    return false
                }
            },
        )
    }

    private fun setNovelAdapter() {
        val novelDetail = mNovelDetail!!
        var novelText = novelDetail.getNovel_text()
        if (novelText == null || novelText.isEmpty()) {
            novelText = ""
        }
        if (novelDetail.getParsedChapters() != null && novelDetail.getParsedChapters().isNotEmpty()) {
            val uploadedImageMark = "[uploadedimage:"
            val pixivImageMark = "[pixivimage:"
            if (novelText.contains(uploadedImageMark) || novelText.contains(pixivImageMark)) {
                while (novelText.contains("][")) {
                    novelText = novelText.replace("][", "]\n[")
                }
                val stringArray = novelText.split("\n").toTypedArray()
                val textList = ArrayList(Arrays.asList(*stringArray))
                val holderList = ArrayList<ListItemHolder>()
                holderList.add(SpaceHolder())
                for (s in textList) {
                    if (s.contains(uploadedImageMark)) {
                        var id = 0L
                        val startIndex = s.indexOf(uploadedImageMark) + uploadedImageMark.length
                        val endIndex = s.indexOf("]")
                        try {
                            id = s.substring(startIndex, endIndex).toLong()
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                        holderList.add(
                            NovelImageHolder(
                                NovelImageHolder.Type.UploadedImage,
                                id,
                                0,
                                mWebNovel!!,
                            ),
                        )
                    } else if (s.contains(pixivImageMark)) {
                        var id = 0L
                        var indexInIllust = 0
                        val startIndex = s.indexOf(pixivImageMark) + pixivImageMark.length
                        val endIndex = s.indexOf("]")
                        val result = s.substring(startIndex, endIndex)
                        try {
                            if (result.contains("-")) {
                                val ret = result.split("-")
                                indexInIllust = ret[1].toInt()
                                id = ret[0].toLong()
                            } else {
                                id = result.toLong()
                            }
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                        holderList.add(
                            NovelImageHolder(
                                NovelImageHolder.Type.PixivImage,
                                id,
                                indexInIllust,
                                mWebNovel!!,
                            ),
                        )
                    } else {
                        holderList.add(NovelTextHolder(s, Common.getNovelTextColor()))
                    }
                }
                holderList.add(SpaceHolder())
                holderList.add(TextDescHolder(getString(R.string.string_107)))
                holderList.add(SpaceHolder())
                val commonAdapter = CommonAdapter()
                baseBind.viewPager.adapter = commonAdapter
                commonAdapter.submitList(holderList)
            } else {
                baseBind.viewPager.adapter = VNewAdapter(novelDetail.getParsedChapters(), mContext)
            }
            if (novelDetail.getNovel_marker() != null) {
                val parsedSize = novelDetail.getParsedChapters().size
                var pageIndex =
                    minOf(
                        novelDetail.getNovel_marker().getPage(),
                        novelDetail.getParsedChapters()[parsedSize - 1].getChapterIndex(),
                    )
                pageIndex = maxOf(pageIndex, novelDetail.getParsedChapters()[0].getChapterIndex())
                baseBind.viewPager.scrollToPosition(pageIndex - 1)

                val markerPage = mNovelDetail!!.getNovel_marker().getPage()
                if (markerPage > 0) {
                    baseBind.saveNovel.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.novel_marker_add))
                } else {
                    baseBind.saveNovel.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.novel_marker_none))
                }
            } else {
                baseBind.saveNovel.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.novel_marker_none))
            }

            baseBind.saveNovel.setOnClickListener {
                val someView = baseBind.viewPager.findChildViewUnder(0f, 0f)!!
                val currentPageIndex = baseBind.viewPager.findContainingViewHolder(someView)!!.adapterPosition
                val chapterIndex = mNovelDetail!!.getParsedChapters()[currentPageIndex].getChapterIndex()
                PixivOperate.postNovelMarker(
                    mNovelDetail!!.getNovel_marker(),
                    mNovelBean!!.getId(),
                    chapterIndex,
                    baseBind.saveNovel,
                )
            }
        } else {
            if (novelDetail.getNovel_text().contains("[newpage]")) {
                val partList = novelDetail.getNovel_text().split("\\[newpage]".toRegex()).toTypedArray()
                baseBind.viewPager.adapter = VAdapter(Arrays.asList(*partList), mContext)
            } else {
                baseBind.viewPager.adapter =
                    VAdapter(Collections.singletonList(novelDetail.getNovel_text()), mContext)
            }
        }
    }
}
