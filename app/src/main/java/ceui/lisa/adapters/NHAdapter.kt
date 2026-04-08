package ceui.lisa.adapters

import android.content.Context
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyRankNovelHorizontalBinding
import ceui.lisa.models.NovelBean
import ceui.lisa.utils.GlideUtil
import java.util.Locale

class NHAdapter(targetList: List<NovelBean>, context: Context) :
    BaseAdapter<NovelBean, RecyRankNovelHorizontalBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_rank_novel_horizontal
    }

    override fun bindData(target: NovelBean, bindView: ViewHolder<RecyRankNovelHorizontalBinding>, position: Int) {
        bindView.baseBind.novelLength.text =
            String.format(Locale.getDefault(), "%d字", allItems[position].text_length)
        bindView.baseBind.title.text = allItems[position].title
        bindView.baseBind.author.text = allItems[position].user.name
        Glide.with(mContext).load(GlideUtil.getUrl(allItems[position].image_urls.medium))
            .placeholder(R.color.light_bg).into(bindView.baseBind.illustImage)
        Glide.with(mContext).load(GlideUtil.getUrl(allItems[position].user.profile_image_urls.medium))
            .placeholder(R.color.light_bg).into(bindView.baseBind.userHead)
        if (mOnItemClickListener != null) {
            bindView.itemView.setOnClickListener { v -> mOnItemClickListener.onItemClick(v, position, 0) }
        }
    }
}
