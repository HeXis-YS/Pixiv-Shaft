package ceui.lisa.adapters

import android.content.Context
import android.text.TextUtils
import ceui.lisa.R
import ceui.lisa.databinding.RecySelectTagBinding
import ceui.lisa.models.TagsBean

class SAdapter(targetList: List<TagsBean>, context: Context) :
    BaseAdapter<TagsBean, RecySelectTagBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_select_tag
    }

    override fun bindData(target: TagsBean, bindView: ViewHolder<RecySelectTagBinding>, position: Int) {
        val tagName = allItems[position].name
        val translatedTagName = allItems[position].translated_name
        var finalTagName = tagName
        if (!TextUtils.isEmpty(translatedTagName)) {
            finalTagName = String.format("%s/%s", tagName, translatedTagName)
        }
        bindView.baseBind.starSize.text = finalTagName

        bindView.baseBind.illustCount.setOnCheckedChangeListener { _, isChecked ->
            allItems[position].setSelectedLocalAndRemote(isChecked)
        }

        bindView.baseBind.illustCount.isChecked = allItems[position].isSelectedLocalOrRemote
        bindView.itemView.setOnClickListener { bindView.baseBind.illustCount.performClick() }
    }
}
