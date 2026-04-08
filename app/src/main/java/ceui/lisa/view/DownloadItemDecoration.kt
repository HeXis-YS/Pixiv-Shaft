package ceui.lisa.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DownloadItemDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (column == 0 || column == 1) {
            outRect.top = 0
            outRect.left = 0
            outRect.right = spacing
            outRect.bottom = spacing
        }

        if (column == 3) {
            outRect.top = 0
            outRect.left = 0
            outRect.right = 0
            outRect.bottom = spacing
        }
    }
}
