package ceui.lisa.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ceui.lisa.R
import ceui.lisa.activities.BaseActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.databinding.RecyIllustDetailBinding
import ceui.lisa.download.IllustDownload
import ceui.lisa.models.IllustsBean
import ceui.lisa.transformer.LargeBitmapScaleTransformer
import ceui.lisa.transformer.UniformScaleTransformation
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUrlChild
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.ProgressManager
import me.jessyan.progressmanager.body.ProgressInfo

class IllustAdapter(
    private val mActivity: FragmentActivity,
    private val mFragment: Fragment?,
    illustsBean: IllustsBean,
    private val maxHeight: Int,
    isForceOriginal: Boolean,
) : AbstractIllustAdapter<ViewHolder<RecyIllustDetailBinding>>() {

    init {
        Common.showLog("IllustAdapter maxHeight $maxHeight")
        mContext = mFragment!!.requireContext()
        allIllust = illustsBean
        imageSize = mContext!!.resources.displayMetrics.widthPixels
        this.isForceOriginal = isForceOriginal
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder<RecyIllustDetailBinding> {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.recy_illust_detail,
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder<RecyIllustDetailBinding>,
        position: Int,
    ) {
        super.onBindViewHolder(holder, position)
        val illust = allIllust!!
        if (longPressDownload && mActivity is BaseActivity<*>) {
            holder.itemView.setOnLongClickListener {
                IllustDownload.downloadIllustCertainPage(illust, position, mActivity)
                if (Shaft.sSettings.isAutoPostLikeWhenDownload && !illust.isIs_bookmarked) {
                    PixivOperate.postLikeDefaultStarType(illust)
                }
                true
            }
        }

        if (position == 0) {
            if (illust.page_count == 1) {
                val screenRatio = imageSize.toFloat() / maxHeight
                val illustRatio = illust.width.toFloat() / illust.height

                if (kotlin.math.abs(illustRatio - screenRatio) < 0.1f) {
                    holder.baseBind.illust.scaleType = ImageView.ScaleType.CENTER_CROP

                    val params = holder.baseBind.illust.layoutParams
                    params.width = imageSize
                    params.height = maxHeight
                    Common.showLog("onBindViewHolder $maxHeight")
                    holder.baseBind.illust.layoutParams = params
                    loadIllust(holder, position, false)
                } else if (illustRatio < screenRatio) {
                    holder.baseBind.illust.scaleType = ImageView.ScaleType.CENTER_CROP
                    loadIllust(holder, position, true)
                } else {
                    holder.baseBind.illust.scaleType = ImageView.ScaleType.FIT_CENTER

                    val params = holder.baseBind.illust.layoutParams
                    params.width = imageSize
                    params.height = maxHeight
                    holder.baseBind.illust.layoutParams = params
                    loadIllust(holder, position, false)
                }
            } else {
                holder.baseBind.illust.scaleType = ImageView.ScaleType.CENTER_CROP
                loadIllust(holder, position, true)
            }
        } else {
            holder.baseBind.illust.scaleType = ImageView.ScaleType.CENTER_CROP
            loadIllust(holder, position, true)
        }
    }

    private fun loadIllust(
        holder: ViewHolder<RecyIllustDetailBinding>,
        position: Int,
        changeSize: Boolean,
    ) {
        holder.baseBind.reload.setOnClickListener {
            holder.baseBind.reload.visibility = View.GONE
            holder.baseBind.progressLayout.donutProgress.visibility = View.VISIBLE
            loadIllust(holder, position, changeSize)
        }
        val illust = allIllust!!
        val isLoadOriginalImage = Shaft.sSettings.isShowOriginalPreviewImage() || isForceOriginal
        val imageUrl =
            if (isLoadOriginalImage) {
                IllustDownload.getUrl(illust, position, Params.IMAGE_RESOLUTION_ORIGINAL)
            } else {
                IllustDownload.getUrl(illust, position, Params.IMAGE_RESOLUTION_LARGE)
            }
        ProgressManager.getInstance().addResponseListener(
            imageUrl,
            object : ProgressListener {
                override fun onProgress(progressInfo: ProgressInfo) {
                    holder.baseBind.progressLayout.donutProgress.progress = progressInfo.percent.toFloat()
                    if (progressInfo.isFinish) {
                        ProgressManager.getInstance().removeResponseListener(imageUrl, this)
                    }
                }

                override fun onError(id: Long, e: Exception) = Unit
            },
        )

        val requestManager: RequestManager =
            if (mFragment != null) {
                Glide.with(mFragment)
            } else {
                Glide.with(mContext!!)
            }

        requestManager
            .asBitmap()
            .load(GlideUrlChild(imageUrl))
            .transform(LargeBitmapScaleTransformer())
            .transition(BitmapTransitionOptions.withCrossFade())
            .listener(
                object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        holder.baseBind.reload.visibility = View.VISIBLE
                        holder.baseBind.progressLayout.donutProgress.visibility = View.INVISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        holder.baseBind.reload.visibility = View.GONE
                        holder.baseBind.progressLayout.donutProgress.visibility = View.INVISIBLE
                        if (isLoadOriginalImage) {
                            Shaft.getMMKV().encode(imageUrl, true)
                        }
                        return false
                    }
                },
            )
            .into(UniformScaleTransformation(holder.baseBind.illust, changeSize))
    }

    companion object {
        private val longPressDownload = Shaft.sSettings.isIllustLongPressDownload()
    }
}
