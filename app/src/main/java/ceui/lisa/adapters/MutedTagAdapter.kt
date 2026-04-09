package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import ceui.lisa.R
import ceui.lisa.databinding.RecyMutedTagBinding
import ceui.lisa.models.TagsBean
import ceui.lisa.utils.PixivOperate

class MutedTagAdapter(
    targetList: List<TagsBean>,
    context: Context,
) : BaseAdapter<TagsBean, RecyMutedTagBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_muted_tag
    }

    override fun bindData(target: TagsBean, bindView: ViewHolder<RecyMutedTagBinding>, position: Int) {
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

        bindView.baseBind.sideDecorator.visibility =
            if (allItems[position].filter_mode != 0) View.VISIBLE else View.GONE

        bindView.baseBind.isEffective.setOnCheckedChangeListener(null)
        bindView.baseBind.isEffective.isChecked = target.isEffective
        bindView.baseBind.isEffective.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { _, isChecked ->
                target.isEffective = isChecked
                PixivOperate.updateTag(target)
            },
        )

        val listener = mOnItemClickListener
        if (listener != null) {
            bindView.baseBind.deleteItem.setOnClickListener { v ->
                listener.onItemClick(v, position, 1)
            }
            bindView.itemView.setOnClickListener { v -> listener.onItemClick(v, position, 0) }
        }
    }
}
