package ceui.lisa.adapters

import android.content.Context
import android.view.View
import ceui.lisa.R
import ceui.lisa.databinding.FragmentSingleNovelBinding
import ceui.lisa.models.NovelDetail
import ceui.lisa.utils.Common

class VNewAdapter(targetList: List<NovelDetail.NovelChapterBean>, context: Context) :
    BaseAdapter<NovelDetail.NovelChapterBean, FragmentSingleNovelBinding>(targetList, context) {

    private val textColor: Int = Common.getNovelTextColor()

    override fun initLayout() {
        mLayoutID = R.layout.fragment_single_novel
    }

    override fun bindData(
        target: NovelDetail.NovelChapterBean,
        bindView: ViewHolder<FragmentSingleNovelBinding>,
        position: Int,
    ) {
        val chapterContent = target.chapterContent

        if (position == 0) {
            bindView.baseBind.head.visibility = View.VISIBLE
        } else {
            bindView.baseBind.head.visibility = View.GONE
        }

        bindView.baseBind.chapter.text = target.chapterName

        if (position == allItems.size - 1) {
            bindView.baseBind.bottom.visibility = View.VISIBLE
            bindView.baseBind.endText.visibility = View.VISIBLE
        } else {
            bindView.baseBind.bottom.visibility = View.GONE
            bindView.baseBind.endText.visibility = View.GONE
        }

        bindView.baseBind.partIndex.visibility = View.GONE
        bindView.baseBind.novelDetail.text = chapterContent

        bindView.baseBind.chapter.setTextColor(textColor)
        bindView.baseBind.novelDetail.setTextColor(textColor)
        bindView.baseBind.endText.setTextColor(textColor)
    }
}
