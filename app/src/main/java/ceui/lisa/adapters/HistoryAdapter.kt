package ceui.lisa.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.database.IllustHistoryEntity
import ceui.lisa.databinding.RecyViewHistoryBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.NovelBean
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.GlideUtil
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    targetList: List<IllustHistoryEntity>,
    context: Context,
) : BaseAdapter<IllustHistoryEntity, RecyViewHistoryBinding>(targetList, context) {

    private var illustImageSize = 0
    private var novelImageSize = 0
    private val mTime =
        SimpleDateFormat(
            mContext.resources.getString(R.string.string_350),
            Locale.getDefault(),
        )

    init {
        illustImageSize =
            (mContext.resources.displayMetrics.widthPixels -
                mContext.resources.getDimensionPixelSize(R.dimen.four_dp)) / 2
        novelImageSize = DensityUtil.dp2px(110.0f)
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_view_history
    }

    @SuppressLint("SetTextI18n")
    override fun bindData(
        target: IllustHistoryEntity,
        bindView: ViewHolder<RecyViewHistoryBinding>,
        position: Int,
    ) {
        if (target.type == 0) {
            val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
            params.height = illustImageSize
            params.width = illustImageSize
            bindView.baseBind.illustImage.layoutParams = params

            val current = Shaft.sGson.fromJson(allItems[position].illustJson, IllustsBean::class.java)
            Glide.with(mContext)
                .load(GlideUtil.getMediumImg(current))
                .placeholder(R.color.light_bg)
                .into(bindView.baseBind.illustImage)
            bindView.baseBind.title.text = current.title
            bindView.baseBind.author.text = String.format("by: %s", current.user.name)

            if (current.isGif) {
                bindView.baseBind.pSize.visibility = View.VISIBLE
                bindView.baseBind.pSize.text = "GIF"
            } else {
                if (current.page_count == 1) {
                    bindView.baseBind.pSize.visibility = View.GONE
                } else {
                    bindView.baseBind.pSize.visibility = View.VISIBLE
                    bindView.baseBind.pSize.text =
                        String.format(Locale.getDefault(), "%dP", current.page_count)
                }
            }

            val listener = mOnItemClickListener
            if (listener != null) {
                bindView.itemView.setOnClickListener { v ->
                    listener.onItemClick(v, position, 0)
                }
                bindView.baseBind.author.setOnClickListener { _ ->
                    bindView.baseBind.author.tag = current.user.id
                    listener.onItemClick(bindView.baseBind.author, position, 1)
                }
            }
        } else if (target.type == 1) {
            val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
            params.width = novelImageSize
            bindView.baseBind.illustImage.layoutParams = params

            val current = Shaft.sGson.fromJson(allItems[position].illustJson, NovelBean::class.java)
            Glide.with(mContext)
                .load(GlideUtil.getUrl(current.image_urls.medium))
                .placeholder(R.color.light_bg)
                .into(bindView.baseBind.illustImage)
            bindView.baseBind.title.text = current.title
            bindView.baseBind.author.text = String.format("by: %s", current.user.name)

            bindView.baseBind.pSize.visibility = View.VISIBLE
            bindView.baseBind.pSize.text = "小说"

            val listener = mOnItemClickListener
            if (listener != null) {
                bindView.itemView.setOnClickListener { _: View ->
                    TemplateActivity.startNovelDetail(mContext, current)
                }
                bindView.baseBind.author.setOnClickListener { _ ->
                    bindView.baseBind.author.tag = current.user.id
                    listener.onItemClick(bindView.baseBind.author, position, 1)
                }
            }
        }

        bindView.baseBind.time.text = mTime.format(allItems[position].time)
        bindView.baseBind.deleteItem.setOnClickListener { v ->
            mOnItemClickListener?.onItemClick(v, position, 2)
        }
        (bindView as SpringHolder).spring.currentValue = -400.0
        bindView.spring.endValue = 0.0
    }

    override fun getNormalItem(parent: ViewGroup): ViewHolder<RecyViewHistoryBinding> {
        return SpringHolder(
            RecyViewHistoryBinding.inflate(
                LayoutInflater.from(mContext),
                parent,
                false,
            ),
        )
    }
}
