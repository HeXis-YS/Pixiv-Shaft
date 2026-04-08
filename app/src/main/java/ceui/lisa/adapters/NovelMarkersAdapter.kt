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
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.UActivity
import ceui.lisa.databinding.RecyNovelMarkersBinding
import ceui.lisa.models.MarkedNovelItem
import ceui.lisa.models.TagsBean
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import java.util.Locale

class NovelMarkersAdapter(
    targetList: List<MarkedNovelItem>,
    context: Context,
) : BaseAdapter<MarkedNovelItem, RecyNovelMarkersBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_novel_markers
    }

    override fun bindData(
        target: MarkedNovelItem,
        bindView: ViewHolder<RecyNovelMarkersBinding>,
        position: Int,
    ) {
        if (target.novel.series != null && !TextUtils.isEmpty(target.novel.series.title)) {
            bindView.baseBind.series.visibility = View.VISIBLE
            bindView.baseBind.series.text =
                String.format(mContext.getString(R.string.string_184), target.novel.series.title)
            bindView.baseBind.series.setOnClickListener { _: View ->
                TemplateActivity.startNovelSeriesDetail(mContext, target.novel.series.id)
            }
        } else {
            bindView.baseBind.series.visibility = View.GONE
        }
        bindView.baseBind.title.text = target.novel.title
        bindView.baseBind.novelTag.adapter =
            object : TagAdapter<TagsBean>(target.novel.tags) {
                override fun getView(parent: FlowLayout, position: Int, s: TagsBean): View {
                    val tv = LayoutInflater.from(mContext).inflate(
                        R.layout.recy_single_line_text_new,
                        parent,
                        false,
                    ) as TextView
                    val tag = s.name
                    tv.text = tag
                    return tv
                }
            }
        bindView.baseBind.novelTag.setOnTagClickListener(
            object : TagFlowLayout.OnTagClickListener {
                override fun onTagClick(view: View, position: Int, parent: FlowLayout): Boolean {
                    val intent = Intent(mContext, SearchActivity::class.java)
                    intent.putExtra(Params.KEY_WORD, target.novel.tags[position].name)
                    intent.putExtra(Params.INDEX, 1)
                    mContext.startActivity(intent)
                    return true
                }
            },
        )
        bindView.baseBind.author.text = target.novel.user.name
        bindView.baseBind.howManyWord.text =
            String.format(Locale.getDefault(), "%d字", target.novel.text_length)
        bindView.baseBind.bookmarkCount.text = target.novel.total_bookmarks.toString()
        Glide.with(mContext).load(GlideUtil.getUrl(target.novel.image_urls.maxImage))
            .into(bindView.baseBind.cover)
        Glide.with(mContext).load(GlideUtil.getHead(target.novel.user))
            .into(bindView.baseBind.userHead)

        bindView.baseBind.cover.setOnClickListener { _: View ->
            TemplateActivity.startImageDetail(
                mContext,
                GlideUtil.getUrl(target.novel.image_urls.maxImage).toStringUrl(),
            )
        }

        bindView.baseBind.userHead.setOnClickListener { _: View ->
            val intent = Intent(mContext, UActivity::class.java)
            intent.putExtra(Params.USER_ID, target.novel.user.id)
            mContext.startActivity(intent)
        }

        bindView.baseBind.author.setOnClickListener { _: View ->
            val intent = Intent(mContext, UActivity::class.java)
            intent.putExtra(Params.USER_ID, target.novel.user.id)
            mContext.startActivity(intent)
        }

        bindView.itemView.setOnClickListener { _: View ->
            TemplateActivity.startNovelDetail(mContext, target.novel)
        }

        bindView.baseBind.mark.setOnClickListener { _ ->
            PixivOperate.postNovelMarker(
                target.novel_marker,
                target.novel.id,
                bindView.baseBind.mark,
            )
        }
    }
}
