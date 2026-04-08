package ceui.lisa.adapters

import android.content.Context
import android.view.View
import ceui.lisa.R
import ceui.lisa.databinding.FragmentSingleNovelBinding
import java.util.Locale

class VAdapter(
    targetList: List<String>,
    context: Context,
) : BaseAdapter<String, FragmentSingleNovelBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.fragment_single_novel
    }

    override fun bindData(
        target: String,
        bindView: ViewHolder<FragmentSingleNovelBinding>,
        position: Int,
    ) {
        var content = target
        if (position == 0) {
            if (content.contains("[chapter:")) {
                bindView.baseBind.chapter.visibility = View.VISIBLE
                val start = content.indexOf("[chapter:") + 9
                val end = content.indexOf("]") + 1
                bindView.baseBind.chapter.text = content.substring(start, end - 1)
                content = content.substring(end)
            } else {
                bindView.baseBind.chapter.visibility = View.GONE
            }
            bindView.baseBind.head.visibility = View.VISIBLE
        } else {
            bindView.baseBind.chapter.visibility = View.GONE
            bindView.baseBind.head.visibility = View.GONE
        }
        if (position == allItems.size - 1) {
            bindView.baseBind.bottom.visibility = View.VISIBLE
            bindView.baseBind.endText.visibility = View.VISIBLE
        } else {
            bindView.baseBind.bottom.visibility = View.GONE
            bindView.baseBind.endText.visibility = View.GONE
        }
        if (allItems.size == 1) {
            bindView.baseBind.partIndex.visibility = View.GONE
        } else {
            bindView.baseBind.partIndex.visibility = View.VISIBLE
            bindView.baseBind.partIndex.text =
                String.format(Locale.getDefault(), " --- Part %d --- ", position + 1)
        }
        bindView.baseBind.novelDetail.text = content
    }
}
