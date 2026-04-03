package ceui.lisa.interfaces

import android.view.View

fun interface OnItemClickListener {
    fun onItemClick(v: View?, position: Int, viewType: Int)
}
