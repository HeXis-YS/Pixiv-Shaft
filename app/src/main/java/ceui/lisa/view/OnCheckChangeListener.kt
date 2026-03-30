package ceui.lisa.view

import android.view.View

interface OnCheckChangeListener {

    fun onSelect(index: Int, view: View)

    fun onReselect(index: Int, view: View)
}
