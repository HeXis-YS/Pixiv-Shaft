package ceui.lisa.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import ceui.lisa.databinding.RecyRecmdHeaderBinding
import ceui.lisa.models.NovelBean

class NAdapterWithHeadView(targetList: List<NovelBean>, context: Context) :
    NAdapter(targetList, context) {

    private var novelHeader: NovelHeader? = null

    override fun headerSize(): Int {
        return 1
    }

    override fun getHeader(parent: ViewGroup): ViewHolder<*> {
        novelHeader = NovelHeader(
            RecyRecmdHeaderBinding.inflate(
                LayoutInflater.from(mContext),
                null,
                false,
            ),
        )
        novelHeader?.initView(mContext)
        return novelHeader!!
    }

    fun setHeadData(novelBeans: List<NovelBean>) {
        novelHeader?.show(mContext, novelBeans)
    }
}
