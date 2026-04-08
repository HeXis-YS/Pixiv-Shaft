package ceui.lisa.adapters

import android.content.Context
import ceui.lisa.R
import ceui.lisa.databinding.RecyStringBinding

class StringAdapter(targetList: List<String>?, context: Context) :
    BaseAdapter<String, RecyStringBinding>(targetList, context) {

    override fun initLayout() {
        mLayoutID = R.layout.recy_string
    }

    override fun bindData(target: String, bindView: ViewHolder<RecyStringBinding>, position: Int) {
        bindView.baseBind.content.text = target
    }
}
