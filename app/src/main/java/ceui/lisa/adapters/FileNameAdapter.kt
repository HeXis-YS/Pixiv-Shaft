package ceui.lisa.adapters

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import androidx.annotation.Nullable
import ceui.lisa.R
import ceui.lisa.databinding.RecyFileNameBinding
import ceui.lisa.download.FileCreator
import ceui.lisa.model.CustomFileNameCell

class FileNameAdapter(
    @Nullable targetList: List<CustomFileNameCell>?,
    context: Context,
) : BaseAdapter<CustomFileNameCell, RecyFileNameBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_file_name
    }

    override fun bindData(
        target: CustomFileNameCell,
        bindView: ViewHolder<RecyFileNameBinding>,
        position: Int,
    ) {
        bindView.baseBind.title.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { _, isChecked ->
                target.isChecked = isChecked
                if (mOnItemClickListener != null) {
                    mOnItemClickListener?.onItemClick(bindView.itemView, position, 0)
                }
            },
        )

        bindView.baseBind.title.isChecked = target.isChecked
        bindView.baseBind.title.text = target.title
        bindView.baseBind.description.text = target.desc
        bindView.itemView.setOnClickListener { _: View -> bindView.baseBind.title.performClick() }

        val enabled = target.code != FileCreator.ILLUST_ID && target.code != FileCreator.P_SIZE
        bindView.baseBind.title.isEnabled = enabled
        bindView.itemView.isEnabled = enabled
    }

    fun unCheckAll() {
        for (customFileNameCell in allItems) {
            customFileNameCell.isChecked = false
        }
        notifyDataSetChanged()
    }
}
