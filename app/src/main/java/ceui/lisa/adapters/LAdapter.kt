package ceui.lisa.adapters

import android.content.Context
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyCardIllustBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.GlideUtil

class LAdapter(targetList: List<IllustsBean>, context: Context) :
    BaseAdapter<IllustsBean, RecyCardIllustBinding>(targetList, context) {

    private val imageSize: Int = mContext.resources.displayMetrics.widthPixels / 3

    fun getImageSize(): Int {
        return imageSize
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_card_illust
    }

    override fun bindData(target: IllustsBean, bindView: ViewHolder<RecyCardIllustBinding>, position: Int) {
        val params: ViewGroup.LayoutParams = bindView.baseBind.imageView.layoutParams
        params.width = imageSize
        params.height = imageSize
        bindView.baseBind.imageView.layoutParams = params
        Glide.with(mContext)
            .load(GlideUtil.getMediumImg(target))
            .placeholder(R.color.second_light_bg)
            .into(bindView.baseBind.imageView)
        bindView.itemView.setOnClickListener { view ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position, 0)
            }
        }
    }
}
