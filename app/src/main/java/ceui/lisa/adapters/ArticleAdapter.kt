package ceui.lisa.adapters

import android.content.Context
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyArticalBinding
import ceui.lisa.models.SpotlightArticlesBean
import ceui.lisa.utils.GlideUtil

class ArticleAdapter(targetList: List<SpotlightArticlesBean>, context: Context) :
    BaseAdapter<SpotlightArticlesBean, RecyArticalBinding>(targetList, context) {

    private val imageSize: Int =
        mContext.resources.displayMetrics.widthPixels -
            2 * mContext.resources.getDimensionPixelSize(R.dimen.sixteen_dp)

    override fun initLayout() {
        mLayoutID = R.layout.recy_artical
    }

    override fun bindData(
        target: SpotlightArticlesBean,
        bindView: ViewHolder<RecyArticalBinding>,
        position: Int,
    ) {
        val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
        params.height = imageSize * 7 / 10
        params.width = imageSize
        bindView.baseBind.illustImage.layoutParams = params
        bindView.baseBind.title.text = target.title

        Glide.with(mContext).load(GlideUtil.getUrl(target.thumbnail))
            .into(bindView.baseBind.illustImage)
        val listener = mOnItemClickListener
        if (listener != null) {
            bindView.itemView.setOnClickListener { v ->
                listener.onItemClick(v, position, 0)
            }
        }
    }
}
