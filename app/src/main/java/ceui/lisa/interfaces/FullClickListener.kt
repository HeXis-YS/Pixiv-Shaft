package ceui.lisa.interfaces

import android.view.View

// 支持点击、长按，OnItemClickListener 只支持点击
interface FullClickListener {
    fun onItemClick(v: View?, position: Int, viewType: Int)

    fun onItemLongClick(v: View?, position: Int, viewType: Int)
}
