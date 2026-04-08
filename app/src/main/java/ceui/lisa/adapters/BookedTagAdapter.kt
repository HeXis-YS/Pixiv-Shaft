package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import ceui.lisa.R
import ceui.lisa.databinding.RecyBookTagBinding
import ceui.lisa.models.TagsBean

class BookedTagAdapter(targetList: List<TagsBean>, context: Context, muted: Boolean) :
    BaseAdapter<TagsBean, RecyBookTagBinding>(targetList, context) {

    private val isMuted: Boolean = muted

    override fun initLayout() {
        mLayoutID = R.layout.recy_book_tag
    }

    override fun bindData(target: TagsBean, bindView: ViewHolder<RecyBookTagBinding>, position: Int) {
        if (TextUtils.isEmpty(allItems[position].name)) {
            bindView.baseBind.starSize.setText(R.string.string_155)
        } else {
            if (!TextUtils.isEmpty(allItems[position].translated_name)) {
                bindView.baseBind.starSize.text =
                    String.format("#%s/%s", allItems[position].name, allItems[position].translated_name)
            } else {
                bindView.baseBind.starSize.text = String.format("#%s", allItems[position].name)
            }
        }

        if (allItems[position].count == -1) {
            bindView.baseBind.illustCount.text = ""
        } else {
            if (isMuted) {
                bindView.baseBind.illustCount.setText(R.string.string_157)
                bindView.baseBind.illustCount.setOnClickListener { v ->
                    mOnItemClickListener.onItemClick(v, position, 1)
                }
            } else {
                bindView.baseBind.illustCount.text =
                    mContext.getString(R.string.string_156, allItems[position].count)
            }
        }
        if (mOnItemClickListener != null) {
            bindView.itemView.setOnClickListener { v -> mOnItemClickListener.onItemClick(v, position, 0) }
        }
    }
}
