package ceui.lisa.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import ceui.lisa.R
import ceui.lisa.databinding.RecyNineBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.DensityUtil

class NineAdapter(
    @Nullable targetList: List<GlideUrl>?,
    context: Context,
    private val illust: IllustsBean,
) : BaseAdapter<GlideUrl, RecyNineBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_nine
    }

    override fun bindData(target: GlideUrl, bindView: ViewHolder<RecyNineBinding>, position: Int) {
        val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
        if (illust.page_count == 1) {
            val imageSize = mContext.resources.displayMetrics.widthPixels - DensityUtil.dp2px(32.0f)
            params.width = imageSize
            params.height = imageSize * illust.height / illust.width
        } else if (illust.page_count == 2 || illust.page_count == 4) {
            val imageSize = (mContext.resources.displayMetrics.widthPixels - DensityUtil.dp2px(20.0f)) / 2
            params.width = imageSize
            params.height = imageSize
        } else {
            val imageSize = (mContext.resources.displayMetrics.widthPixels - DensityUtil.dp2px(32.0f)) / 3
            params.width = imageSize
            params.height = imageSize
        }
        bindView.baseBind.illustImage.layoutParams = params
        bindView.baseBind.illustImage.isNestedScrollingEnabled = true
        Glide.with(mContext).load(target)
            .placeholder(R.color.light_bg)
            .into(bindView.baseBind.illustImage)
    }
}
