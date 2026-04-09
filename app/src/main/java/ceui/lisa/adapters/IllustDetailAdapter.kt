package ceui.lisa.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

/**
 * 作品详情页竖向多P列表
 */
class IllustDetailAdapter : AbstractIllustAdapter<RecyclerView.ViewHolder> {

    private var mFragment: Fragment? = null

    constructor(list: IllustsBean, context: Context, isForceOriginal: Boolean) {
        mContext = context
        allIllust = list
        this.isForceOriginal = isForceOriginal
        val currentContext = mContext!!
        imageSize =
            currentContext.resources.displayMetrics.widthPixels -
                2 * currentContext.resources.getDimensionPixelSize(R.dimen.twelve_dp)
    }

    constructor(list: IllustsBean, context: Context) : this(list, context, false)

    constructor(fragment: Fragment, list: IllustsBean) : this(fragment, list, false)

    constructor(fragment: Fragment, list: IllustsBean, isForceOriginal: Boolean) {
        mFragment = fragment
        mContext = fragment.requireContext()
        allIllust = list
        this.isForceOriginal = isForceOriginal
        val currentContext = mContext!!
        imageSize =
            currentContext.resources.displayMetrics.widthPixels -
                2 * currentContext.resources.getDimensionPixelSize(R.dimen.twelve_dp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TagHolder(
            LayoutInflater.from(mContext).inflate(R.layout.recy_illust_grid, parent, false),
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val currentOne = holder as TagHolder
        val currentContext = mContext!!
        val currentIllust = allIllust!!
        Common.showLog("IllustDetailAdapter onBindViewHolder 000")
        val isLoadOriginalImage = Shaft.sSettings.isShowOriginalPreviewImage() || isForceOriginal
        val imageUrl: GlideUrl =
            if (isLoadOriginalImage) {
                GlideUtil.getOriginalImage(currentIllust, position)
            } else {
                GlideUtil.getLargeImage(currentIllust, position)
            }
        val requestManager: RequestManager =
            if (mFragment != null) {
                Glide.with(mFragment!!)
            } else {
                Glide.with(currentContext)
            }
        if (position == 0) {
            val params = currentOne.illust.layoutParams
            params.height = imageSize * currentIllust.height / currentIllust.width
            params.width = imageSize
            currentOne.illust.layoutParams = params
            requestManager
                .asDrawable()
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?,
                    ) {
                        currentOne.illust.setImageDrawable(resource)
                        if (isLoadOriginalImage) {
                            Shaft.getMMKV().encode(imageUrl.toStringUrl(), true)
                        }
                    }
                })
        } else {
            requestManager
                .asBitmap()
                .load(imageUrl)
                .transition(withCrossFade())
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?,
                    ) {
                        val params = currentOne.illust.layoutParams
                        params.width = imageSize
                        params.height = imageSize * resource.height / resource.width
                        currentOne.illust.layoutParams = params
                        currentOne.illust.setImageBitmap(resource)
                        if (isLoadOriginalImage) {
                            Shaft.getMMKV().encode(imageUrl.toStringUrl(), true)
                        }
                    }
                })
        }
    }

    class TagHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val illust: ImageView = itemView.findViewById(R.id.illust_image)
    }
}
