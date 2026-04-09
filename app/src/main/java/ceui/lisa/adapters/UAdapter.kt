package ceui.lisa.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecyUserPreviewBinding
import ceui.lisa.interfaces.FullClickListener
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.NovelBean
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import java.io.Serializable
import java.util.Arrays

class UAdapter(
    @Nullable targetList: List<UserPreviewsBean>?,
    context: Context,
) : BaseAdapter<UserPreviewsBean, RecyUserPreviewBinding>(targetList, context) {

    private val imageSize: Int =
        (mContext.resources.displayMetrics.widthPixels -
            2 * mContext.resources.getDimensionPixelSize(R.dimen.eight_dp)) / 3
    private var mFullClickListener: FullClickListener? = null

    init {
        handleClick()
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_user_preview
    }

    override fun bindData(
        target: UserPreviewsBean,
        bindView: ViewHolder<RecyUserPreviewBinding>,
        position: Int,
    ) {
        val params: ViewGroup.LayoutParams = bindView.baseBind.userShowOne.layoutParams
        params.height = imageSize
        params.width = imageSize
        bindView.baseBind.userShowOne.layoutParams = params
        bindView.baseBind.userShowTwo.layoutParams = params
        bindView.baseBind.userShowThree.layoutParams = params
        bindView.baseBind.userName.text = target.user.name
        bindView.baseBind.userShowOne.setImageResource(android.R.color.transparent)
        bindView.baseBind.userShowTwo.setImageResource(android.R.color.transparent)
        bindView.baseBind.userShowThree.setImageResource(android.R.color.transparent)

        val views: List<ImageView> =
            Arrays.asList(
                bindView.baseBind.userShowOne,
                bindView.baseBind.userShowTwo,
                bindView.baseBind.userShowThree,
            )
        val shows = ArrayList<Serializable>(target.illusts.subList(0, minOf(3, target.illusts.size)))
        if (shows.size < 3) {
            shows.addAll(target.novels.subList(0, minOf(3 - shows.size, target.novels.size)))
        }
        for (i in 0..2) {
            val item: Serializable? = if (i < shows.size) shows[i] else null
            val model =
                when (item) {
                    is IllustsBean -> GlideUtil.getMediumImg(shows[i] as IllustsBean)
                    is NovelBean -> GlideUtil.getUrl((shows[i] as NovelBean).image_urls.medium)
                    else -> null
                }
            Glide.with(mContext)
                .load(model)
                .placeholder(R.color.light_bg)
                .into(views[i])
        }

        Glide.with(mContext)
            .load(GlideUtil.getUrl(allItems[position].user.profile_image_urls.medium))
            .error(R.drawable.no_profile)
            .into(bindView.baseBind.userHead)
        bindView.baseBind.postLikeUser.text =
            if (allItems[position].user.isIs_followed) {
                mContext.getString(R.string.post_unfollow)
            } else {
                mContext.getString(R.string.post_follow)
            }

        if (mFullClickListener != null) {
            bindView.itemView.setOnClickListener { v ->
                mFullClickListener!!.onItemClick(v, position, 0)
            }

            bindView.baseBind.postLikeUser.setOnClickListener {
                mFullClickListener!!.onItemClick(bindView.baseBind.postLikeUser, position, 1)
            }

            bindView.baseBind.postLikeUser.setOnLongClickListener { _: View ->
                mFullClickListener!!.onItemLongClick(bindView.baseBind.postLikeUser, position, 1)
                true
            }
        }
    }

    fun setFullClickListener(fullClickListener: FullClickListener): UAdapter {
        mFullClickListener = fullClickListener
        return this
    }

    private fun handleClick() {
        setFullClickListener(
            object : FullClickListener {
                override fun onItemClick(v: View?, position: Int, viewType: Int) {
                    if (viewType == 0) {
                        Common.showUser(mContext, allItems[position])
                    } else if (viewType == 1) {
                        if (allItems[position].user.isIs_followed) {
                            PixivOperate.postUnFollowUser(allItems[position].user.id)
                            val postFollow = v as Button
                            allItems[position].user.isIs_followed = false
                            postFollow.text = mContext.getString(R.string.post_follow)
                        } else {
                            PixivOperate.postFollowUser(allItems[position].user.id, Params.TYPE_PUBLIC)
                            allItems[position].user.isIs_followed = true
                            val postFollow = v as Button
                            postFollow.text = mContext.getString(R.string.post_unfollow)
                        }
                    }
                }

                override fun onItemLongClick(v: View?, position: Int, viewType: Int) {
                    PixivOperate.postFollowUser(allItems[position].user.id, Params.TYPE_PRIVATE)
                    val postFollow = v as Button
                    postFollow.text = mContext.getString(R.string.post_unfollow)
                }
            },
        )
    }

    override fun setLiked(id: Int, isLike: Boolean) {
        if (id == 0) {
            return
        }
        if (allItems.isNullOrEmpty()) {
            return
        }

        for (i in allItems.indices) {
            if (allItems[i].user.id == id) {
                allItems[i].user.isIs_followed = isLike
                if (headerSize() != 0) {
                    notifyItemChanged(i + headerSize())
                } else {
                    notifyItemChanged(i)
                }
                break
            }
        }
    }
}
