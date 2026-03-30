package ceui.lisa.adapters

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

open class ViewHolder<BindView : ViewDataBinding>(
    @JvmField var baseBind: BindView
) : RecyclerView.ViewHolder(baseBind.root)
