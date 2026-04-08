package ceui.lisa.adapters

import android.content.Context
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyRankIllustHorizontalBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.GlideUtil

class RAdapter(targetList: List<IllustsBean>, context: Context) :
    BaseAdapter<IllustsBean, RecyRankIllustHorizontalBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_rank_illust_horizontal
    }

    override fun bindData(target: IllustsBean, bindView: ViewHolder<RecyRankIllustHorizontalBinding>, position: Int) {
        bindView.baseBind.title.text = allItems[position].title
        bindView.baseBind.author.text = allItems[position].user.name
        Glide.with(mContext).load(GlideUtil.getUrl(allItems[position].image_urls.medium))
            .placeholder(R.color.light_bg).into(bindView.baseBind.illustImage)
        Glide.with(mContext).load(GlideUtil.getUrl(allItems[position].user.profile_image_urls.medium))
            .placeholder(R.color.light_bg).error(R.drawable.no_profile).into(bindView.baseBind.userHead)
        if (mOnItemClickListener != null) {
            bindView.itemView.setOnClickListener { v -> mOnItemClickListener.onItemClick(v, position, 0) }
        }
    }
}
