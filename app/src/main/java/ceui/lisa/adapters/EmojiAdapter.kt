package ceui.lisa.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.io.IOException
import ceui.lisa.R
import ceui.lisa.databinding.RecyEmojiBinding
import ceui.lisa.model.EmojiItem
import ceui.lisa.utils.Common

class EmojiAdapter(@Nullable targetList: List<EmojiItem>?, context: Context) :
    BaseAdapter<EmojiItem, RecyEmojiBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_emoji
    }

    override fun bindData(target: EmojiItem, bindView: ViewHolder<RecyEmojiBinding>, position: Int) {
        try {
            val inputStream = mContext.assets.open(target.resource)
            val d: Drawable = Drawable.createFromStream(inputStream, null)!!
            d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            Glide.with(mContext)
                .load(d)
                .into(bindView.baseBind.emojiImg)
            Common.showLog("wid: ${d.intrinsicWidth} heightL: ${d.intrinsicHeight}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(HtmlTextView.TAG, "source could not be found: ${target.resource}")
        }
        bindView.itemView.setOnClickListener { v ->
            mOnItemClickListener?.onItemClick(v, position, 0)
        }
    }
}
