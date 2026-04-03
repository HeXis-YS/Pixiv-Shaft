package ceui.lisa.interfaces

import android.view.View

fun interface OnItemLongClickListener {
    fun onItemLongClick(v: View?, position: Int, viewType: Int)
}
