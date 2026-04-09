package ceui.lisa.adapters

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout
import ceui.lisa.R
import ceui.lisa.activities.SearchActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.UActivity
import ceui.lisa.databinding.RecyNovelBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.models.NovelBean
import ceui.lisa.models.TagsBean
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import java.util.Locale

open class NAdapter @JvmOverloads constructor(
    targetList: List<NovelBean>?,
    context: Context,
    private var showShop: Boolean = false,
) : BaseAdapter<NovelBean, RecyNovelBinding>(targetList, context) {

    init {
        handleClick()
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_novel
    }

    override fun bindData(
        target: NovelBean,
        bindView: ViewHolder<RecyNovelBinding>,
        position: Int,
    ) {
        if (target.series != null && !TextUtils.isEmpty(target.series.title)) {
            bindView.baseBind.series.visibility = View.VISIBLE
            bindView.baseBind.series.text =
                String.format(mContext.getString(R.string.string_184), target.series.title)
            if (!showShop) {
                bindView.baseBind.series.setOnClickListener {
                    TemplateActivity.startNovelSeriesDetail(
                        mContext,
                        allItems[position].series.id,
                    )
                }
            }
        } else {
            bindView.baseBind.series.visibility = View.GONE
        }
        bindView.baseBind.title.text =
            if (showShop) {
                String.format(Locale.getDefault(), "#%d %s", position + 1, target.title)
            } else {
                target.title
            }
        bindView.baseBind.novelTag.adapter =
            object : TagAdapter<TagsBean>(target.tags) {
                override fun getView(parent: FlowLayout, position: Int, s: TagsBean): View {
                    val tv =
                        LayoutInflater.from(mContext).inflate(
                            R.layout.recy_single_line_text_new,
                            parent,
                            false,
                        ) as TextView
                    tv.text = s.name
                    return tv
                }
            }
        bindView.baseBind.novelTag.setOnTagClickListener(
            object : TagFlowLayout.OnTagClickListener {
                override fun onTagClick(view: View, position: Int, parent: FlowLayout): Boolean {
                    val intent = Intent(mContext, SearchActivity::class.java)
                    intent.putExtra(Params.KEY_WORD, target.tags[position].name)
                    intent.putExtra(Params.INDEX, 1)
                    mContext.startActivity(intent)
                    return true
                }
            },
        )
        bindView.baseBind.author.text = target.user.name
        bindView.baseBind.howManyWord.text =
            String.format(Locale.getDefault(), "%d字", target.text_length)
        bindView.baseBind.bookmarkCount.text = target.total_bookmarks.toString()
        Glide.with(mContext).load(GlideUtil.getUrl(target.image_urls.maxImage)).into(bindView.baseBind.cover)
        Glide.with(mContext).load(GlideUtil.getHead(target.user)).into(bindView.baseBind.userHead)
        bindView.baseBind.like.setText(
            if (target.isIs_bookmarked) {
                R.string.string_169
            } else {
                R.string.string_170
            },
        )
        if (mOnItemClickListener != null) {
            bindView.baseBind.like.setOnClickListener {
                mOnItemClickListener.onItemClick(bindView.baseBind.like, position, 1)
            }
            bindView.baseBind.cover.setOnClickListener {
                mOnItemClickListener.onItemClick(bindView.baseBind.like, position, 2)
            }
            bindView.baseBind.userHead.setOnClickListener {
                mOnItemClickListener.onItemClick(bindView.baseBind.like, position, 3)
            }
            bindView.baseBind.author.setOnClickListener {
                mOnItemClickListener.onItemClick(bindView.baseBind.like, position, 3)
            }
            bindView.baseBind.like.setOnLongClickListener {
                TemplateActivity.startTagStar(mContext, target.id, Params.TYPE_NOVEL, target.tagNames)
                true
            }
            bindView.itemView.setOnClickListener { v ->
                mOnItemClickListener.onItemClick(v, position, 0)
            }
        }
    }

    private fun handleClick() {
        setOnItemClickListener(
            OnItemClickListener { v, position, viewType ->
                if (viewType == 0) {
                    TemplateActivity.startNovelDetail(mContext, allItems[position])
                } else if (viewType == 1) {
                    PixivOperate.postLikeNovel(allItems[position], Shaft.sUserModel, Params.TYPE_PUBLIC, v)
                } else if (viewType == 2) {
                    TemplateActivity.startImageDetail(
                        mContext,
                        GlideUtil.getUrl(allItems[position].image_urls.maxImage).toStringUrl(),
                    )
                } else if (viewType == 3) {
                    val intent = Intent(mContext, UActivity::class.java)
                    intent.putExtra(Params.USER_ID, allItems[position].user.id)
                    mContext.startActivity(intent)
                }
            },
        )
    }
}
