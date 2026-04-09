package ceui.lisa.adapters

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.core.ImgGetter
import ceui.lisa.databinding.RecyCommentListBinding
import ceui.lisa.models.ReplyCommentBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUrlChild
import ceui.lisa.utils.GlideUtil

class CommentAdapter(
    targetList: List<ReplyCommentBean>,
    context: Context,
) : BaseAdapter<ReplyCommentBean, RecyCommentListBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_comment_list
    }

    override fun bindData(
        target: ReplyCommentBean,
        bindView: ViewHolder<RecyCommentListBinding>,
        position: Int,
    ) {
        Glide.with(mContext).load(GlideUtil.getHead(allItems[position].user))
            .into(bindView.baseBind.userHead)
        bindView.baseBind.userName.text = allItems[position].user.name
        bindView.baseBind.time.text =
            Common.getLocalYYYYMMDDHHMMSSString(allItems[position].date)

        if (!TextUtils.isEmpty(allItems[position].comment)) {
            bindView.baseBind.content.visibility = View.VISIBLE
            bindView.baseBind.content.setHtml(
                allItems[position].commentWithConvertedEmoji,
                ImgGetter(bindView.baseBind.content),
            )
        } else {
            bindView.baseBind.content.visibility = View.GONE
        }

        if (allItems[position].parent_comment != null &&
            allItems[position].parent_comment.user != null
        ) {
            bindView.baseBind.replyContent.visibility = View.VISIBLE

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    mOnItemClickListener?.onItemClick(widget, position, 3)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.parseColor("#507daf")
                }
            }

            val spannableString: SpannableString =
                if (allItems[position].parent_comment.commentWithConvertedEmoji.contains("_2sgsdWB")) {
                    Common.showLog(
                        "Emoji.hasEmoji true " +
                            position +
                            allItems[position].parent_comment.commentWithConvertedEmoji,
                    )
                    SpannableString(
                        Html.fromHtml(
                            String.format(
                                "@%s：%s",
                                allItems[position].parent_comment.user.name,
                                allItems[position].parent_comment.commentWithConvertedEmoji,
                            ),
                            ImgGetter(bindView.baseBind.replyContent),
                            null,
                        ),
                    )
                } else {
                    Common.showLog(
                        "Emoji.hasEmoji false " +
                            position +
                            allItems[position].parent_comment.comment,
                    )
                    SpannableString(
                        String.format(
                            "@%s：%s",
                            allItems[position].parent_comment.user.name,
                            allItems[position].parent_comment.comment,
                        ),
                    )
                }
            spannableString.setSpan(
                clickableSpan,
                0,
                allItems[position].parent_comment.user.name.length + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            bindView.baseBind.replyContent.movementMethod = LinkMovementMethod.getInstance()
            bindView.baseBind.replyContent.text = spannableString
        } else {
            bindView.baseBind.replyContent.visibility = View.GONE
        }

        if (allItems[position].stamp != null && !TextUtils.isEmpty(allItems[position].stamp.stamp_url)) {
            bindView.baseBind.commentImage.visibility = View.VISIBLE
            Glide.with(mContext).load(GlideUrlChild(allItems[position].stamp.stamp_url))
                .into(bindView.baseBind.commentImage)
        } else {
            bindView.baseBind.commentImage.visibility = View.GONE
        }

        val listener = mOnItemClickListener
        if (listener != null) {
            bindView.itemView.setOnClickListener { v ->
                listener.onItemClick(v, position, 0)
            }

            bindView.baseBind.content.setOnClickListener { v ->
                listener.onItemClick(v, position, 0)
            }

            bindView.baseBind.userHead.setOnClickListener { v ->
                listener.onItemClick(v, position, 1)
            }

            bindView.baseBind.userName.setOnClickListener { v ->
                listener.onItemClick(v, position, 1)
            }

            bindView.baseBind.replyContent.setOnClickListener { v ->
                listener.onItemClick(v, position, 2)
            }
        }
    }
}
