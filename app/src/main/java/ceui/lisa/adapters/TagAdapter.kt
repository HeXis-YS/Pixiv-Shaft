package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyTagGridBinding
import ceui.lisa.interfaces.MultiDownload
import ceui.lisa.model.ListTrendingtag
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.GlideUtil

class TagAdapter(
    targetList: List<ListTrendingtag.TrendTagsBean>,
    context: Context,
) : BaseAdapter<ListTrendingtag.TrendTagsBean, RecyTagGridBinding>(targetList, context), MultiDownload {

    override fun initLayout() {
        mLayoutID = R.layout.recy_tag_grid
    }

    override fun bindData(
        target: ListTrendingtag.TrendTagsBean,
        bindView: ViewHolder<RecyTagGridBinding>,
        position: Int,
    ) {
        if (position == 0) {
            bindView.baseBind.illustImage.setHeightRatio(HEADER_RATIO)
            Glide.with(mContext)
                .load(GlideUtil.getLargeImage(allItems[position].illust))
                .placeholder(R.color.light_bg)
                .into(bindView.baseBind.illustImage)
        } else {
            bindView.baseBind.illustImage.setHeightRatio(CONTENT_RATIO)
            Glide.with(mContext)
                .load(GlideUtil.getMediumImg(allItems[position].illust))
                .placeholder(R.color.light_bg)
                .into(bindView.baseBind.illustImage)
        }

        if (TextUtils.isEmpty(allItems[position].translated_name)) {
            bindView.baseBind.chineseTitle.text = ""
        } else {
            bindView.baseBind.chineseTitle.text = String.format("#%s", allItems[position].translated_name)
        }
        bindView.baseBind.title.text = String.format("#%s", allItems[position].getTag())

        bindView.itemView.setOnLongClickListener { _: View ->
            startDownload()
            true
        }
        if (mOnItemClickListener != null) {
            bindView.itemView.setOnClickListener { v -> mOnItemClickListener.onItemClick(v, position, 0) }
        }
    }

    override fun getContext(): Context {
        return mContext
    }

    override fun startDownload() {
        MultiDownload.startMultiDownload(this)
    }

    override fun getIllustList(): List<IllustsBean> {
        val tempList = ArrayList<IllustsBean>()
        for (i in allItems.indices) {
            tempList.add(allItems[i].illust!!)
        }
        return tempList
    }

    private companion object {
        private const val HEADER_RATIO = 0.66f
        private const val CONTENT_RATIO = 1.0f
    }
}
