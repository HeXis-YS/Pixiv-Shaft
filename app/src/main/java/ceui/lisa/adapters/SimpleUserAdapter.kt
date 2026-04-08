package ceui.lisa.adapters

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.databinding.RecySimpleUserBinding
import ceui.lisa.interfaces.FullClickListener
import ceui.lisa.models.UserBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate

class SimpleUserAdapter @JvmOverloads constructor(
    @Nullable targetList: List<UserBean>?,
    context: Context,
    private val isMuteUser: Boolean = false,
) : BaseAdapter<UserBean, RecySimpleUserBinding>(targetList, context) {

    private var mFullClickListener: FullClickListener? = null

    init {
        handleClick()
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_simple_user
    }

    override fun bindData(
        target: UserBean,
        bindView: ViewHolder<RecySimpleUserBinding>,
        position: Int,
    ) {
        bindView.baseBind.userName.text = target.name
        Glide.with(mContext)
            .load(GlideUtil.getUrl(allItems[position].profile_image_urls.medium))
            .error(R.drawable.no_profile)
            .into(bindView.baseBind.userHead)
        bindView.baseBind.postLikeUser.text =
            if (allItems[position].isIs_followed) {
                mContext.getString(R.string.post_unfollow)
            } else {
                mContext.getString(R.string.post_follow)
            }

        if (mFullClickListener != null) {
            bindView.itemView.setOnClickListener { v ->
                mFullClickListener!!.onItemClick(v, position, 0)
            }

            bindView.baseBind.postLikeUser.setOnClickListener { v ->
                mFullClickListener!!.onItemClick(bindView.baseBind.postLikeUser, position, 1)
            }

            bindView.itemView.setOnLongClickListener { _: View ->
                mFullClickListener!!.onItemLongClick(bindView.itemView, position, 0)
                true
            }

            bindView.baseBind.postLikeUser.setOnLongClickListener { _: View ->
                mFullClickListener!!.onItemLongClick(bindView.baseBind.postLikeUser, position, 1)
                true
            }
        }
    }

    fun setFullClickListener(fullClickListener: FullClickListener): SimpleUserAdapter {
        mFullClickListener = fullClickListener
        return this
    }

    private fun handleClick() {
        setFullClickListener(object : FullClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                if (viewType == 0) {
                    Common.showUser(mContext, allItems[position])
                } else if (viewType == 1) {
                    if (allItems[position].isIs_followed) {
                        PixivOperate.postUnFollowUser(allItems[position].id)
                        val postFollow = v as Button
                        allItems[position].isIs_followed = false
                        postFollow.text = mContext.getString(R.string.post_follow)
                    } else {
                        PixivOperate.postFollowUser(allItems[position].id, Params.TYPE_PUBLIC)
                        allItems[position].isIs_followed = true
                        val postFollow = v as Button
                        postFollow.text = mContext.getString(R.string.post_unfollow)
                    }
                }
            }

            override fun onItemLongClick(v: View?, position: Int, viewType: Int) {
                if (isMuteUser && viewType == 0) {
                    PixivOperate.unMuteUser(allItems[position])
                    allItems.removeAt(position)
                    notifyDataSetChanged()
                } else if (viewType == 1) {
                    PixivOperate.postFollowUser(allItems[position].id, Params.TYPE_PRIVATE)
                    val postFollow = v as Button
                    postFollow.text = mContext.getString(R.string.post_unfollow)
                }
            }
        })
    }
}
