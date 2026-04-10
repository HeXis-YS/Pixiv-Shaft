package ceui.lisa.adapters

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class ViewHolder<BindView : ViewBinding>(
    @JvmField var baseBind: BindView
) : RecyclerView.ViewHolder(baseBind.root)
