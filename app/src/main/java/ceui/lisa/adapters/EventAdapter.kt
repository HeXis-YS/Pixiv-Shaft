package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyUserEventBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.GlideUtil

class EventAdapter(
    targetList: List<IllustsBean>,
    context: Context,
) : BaseAdapter<IllustsBean, RecyUserEventBinding>(targetList, context) {

    private val imageSize: Int = mContext.resources.displayMetrics.widthPixels

    override fun initLayout() {
        mLayoutID = R.layout.recy_user_event
    }

    override fun bindData(
        target: IllustsBean,
        bindView: ViewHolder<RecyUserEventBinding>,
        position: Int,
    ) {
        val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
        params.height = imageSize * 2 / 3
        params.width = imageSize
        bindView.baseBind.illustImage.layoutParams = params
        bindView.baseBind.userName.text = allItems[position].user.name
        bindView.baseBind.star.text =
            if (allItems[position].isIs_bookmarked) {
                mContext.getString(R.string.string_179)
            } else {
                mContext.getString(R.string.string_180)
            }
        if (!TextUtils.isEmpty(target.caption)) {
            bindView.baseBind.description.visibility = View.VISIBLE
            bindView.baseBind.description.setHtml(target.caption)
        } else {
            bindView.baseBind.description.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(allItems[position].create_date)) {
            bindView.baseBind.postTime.text =
                String.format("%s发布", allItems[position].create_date.substring(0, 16))
        }

        Glide.with(mContext).load(
            GlideUtil.getUrl(
                allItems[position].user.profile_image_urls.medium,
            ),
        ).into(bindView.baseBind.userHead)
        Glide.with(mContext).load(GlideUtil.getLargeImage(allItems[position]))
            .placeholder(R.color.light_bg)
            .into(bindView.baseBind.illustImage)
        val listener = mOnItemClickListener
        if (listener != null) {
            bindView.itemView.setOnClickListener { v -> listener.onItemClick(v, position, 0) }
            bindView.baseBind.userHead.setOnClickListener { v -> listener.onItemClick(v, position, 1) }
            bindView.baseBind.more.setOnClickListener { v -> listener.onItemClick(v, position, 4) }
            bindView.baseBind.download.setOnClickListener { v -> listener.onItemClick(v, position, 2) }
            bindView.baseBind.star.setOnClickListener { v ->
                bindView.baseBind.star.text =
                    if (allItems[position].isIs_bookmarked) {
                        mContext.getString(R.string.string_180)
                    } else {
                        mContext.getString(R.string.string_179)
                    }
                listener.onItemClick(v, position, 3)
            }
        }
    }
}
