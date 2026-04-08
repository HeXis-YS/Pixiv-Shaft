package ceui.lisa.adapters

import android.content.Context
import ceui.lisa.R
import ceui.lisa.core.IDWithList
import ceui.lisa.databinding.RecyPageBinding
import ceui.lisa.models.IllustsBean

class PageAdapter(targetList: List<IDWithList<IllustsBean>>?, context: Context) :
    BaseAdapter<IDWithList<IllustsBean>, RecyPageBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_page
    }

    override fun bindData(
        target: IDWithList<IllustsBean>,
        bindView: ViewHolder<RecyPageBinding>,
        position: Int,
    ) {
        bindView.baseBind.uuid.text = target.getUUID()
        bindView.baseBind.content.text = target.getList().toString()
    }
}
