package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.database.DownloadEntity
import ceui.lisa.databinding.RecyDownloadedBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.NovelBean
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import java.text.SimpleDateFormat
import java.util.Locale

class DownloadedAdapter(
    targetList: List<DownloadEntity>,
    context: Context,
) : BaseAdapter<DownloadEntity, RecyDownloadedBinding>(targetList, context) {

    private val imageSize: Int = DensityUtil.dp2px(140.0f)
    private val novelImageSize: Int = DensityUtil.dp2px(110.0f)
    private val mTime =
        SimpleDateFormat(
            mContext.resources.getString(R.string.string_350),
            Locale.getDefault(),
        )

    override fun initLayout() {
        mLayoutID = R.layout.recy_downloaded
    }

    override fun bindData(
        target: DownloadEntity,
        bindView: ViewHolder<RecyDownloadedBinding>,
        position: Int,
    ) {
        val fileName = allItems[position].fileName
        if (!TextUtils.isEmpty(fileName) && fileName.contains(Params.NOVEL_KEY)) {
            val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
            params.width = novelImageSize
            bindView.baseBind.illustImage.layoutParams = params

            val current = Shaft.sGson.fromJson(allItems[position].illustGson, NovelBean::class.java)
            bindView.baseBind.illustImage.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(mContext)
                .load(GlideUtil.getUrl(current.image_urls.medium))
                .placeholder(R.color.light_bg)
                .into(bindView.baseBind.illustImage)
            bindView.baseBind.title.text = current.title
            bindView.baseBind.author.text = String.format("by: %s", current.user.name)
            bindView.baseBind.time.text = mTime.format(allItems[position].downloadTime)

            bindView.baseBind.pSize.setText(R.string.string_171)

            val listener = mOnItemClickListener
            if (listener != null) {
                bindView.itemView.setOnClickListener { _: View ->
                    TemplateActivity.startNovelDetail(mContext, current)
                }
                bindView.baseBind.deleteItem.setOnClickListener { v ->
                    listener.onItemClick(v, position, 2)
                }
            }
        } else {
            val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
            params.height = imageSize
            params.width = imageSize
            bindView.baseBind.illustImage.layoutParams = params

            val currentIllust =
                Shaft.sGson.fromJson(allItems[position].illustGson, IllustsBean::class.java)
            if (!TextUtils.isEmpty(allItems[position].fileName) &&
                allItems[position].fileName.contains(".zip")
            ) {
                bindView.baseBind.illustImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                Glide.with(mContext)
                    .load(R.mipmap.zip)
                    .placeholder(R.color.light_bg)
                    .into(bindView.baseBind.illustImage)
            } else {
                if (currentIllust.isGif) {
                    bindView.baseBind.illustImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(mContext)
                        .load(currentIllust.image_urls.medium)
                        .placeholder(R.color.light_bg)
                        .into(bindView.baseBind.illustImage)
                } else {
                    bindView.baseBind.illustImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(mContext)
                        .load(allItems[position].filePath)
                        .placeholder(R.color.light_bg)
                        .into(bindView.baseBind.illustImage)
                }
            }
            bindView.baseBind.title.text = allItems[position].fileName
            bindView.baseBind.author.text = String.format("by: %s", currentIllust.user.name)
            bindView.baseBind.time.text = mTime.format(allItems[position].downloadTime)

            if (currentIllust.page_count == 1) {
                bindView.baseBind.pSize.visibility = View.GONE
            } else {
                bindView.baseBind.pSize.visibility = View.VISIBLE
                bindView.baseBind.pSize.text =
                    String.format(Locale.getDefault(), "%dP", currentIllust.page_count)
            }

            val listener = mOnItemClickListener
            if (listener != null) {
                bindView.itemView.setOnClickListener { v ->
                    listener.onItemClick(v, position, 0)
                }
                bindView.baseBind.author.setOnClickListener { _ ->
                    bindView.baseBind.author.tag = currentIllust.user.id
                    listener.onItemClick(bindView.baseBind.author, position, 1)
                }
                bindView.baseBind.deleteItem.setOnClickListener { v ->
                    listener.onItemClick(v, position, 2)
                }
            }
        }
        (bindView as DownloadedHolder).spring.currentValue = -400.0
        bindView.spring.endValue = 0.0
    }

    override fun getNormalItem(parent: ViewGroup): ViewHolder<RecyDownloadedBinding> {
        return DownloadedHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                mLayoutID,
                parent,
                false,
            ),
        )
    }
}
