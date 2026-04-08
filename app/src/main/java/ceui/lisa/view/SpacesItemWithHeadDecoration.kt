package ceui.lisa.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ceui.lisa.activities.Shaft

class SpacesItemWithHeadDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = space

        var position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.top = 0
            outRect.right = 0
            outRect.left = 0
        } else {
            position--
            val params = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

            if (Shaft.sSettings.lineCount == 2) {
                if (position == 0 || position == 1) {
                    outRect.top = space
                }

                if (params.spanIndex % 2 != 0) {
                    outRect.left = space / 2
                    outRect.right = space
                } else {
                    outRect.left = space
                    outRect.right = space / 2
                }
            } else if (Shaft.sSettings.lineCount == 3) {
                if (position == 0 || position == 1 || position == 2) {
                    outRect.top = space
                }

                if (params.spanIndex % 3 == 0) {
                    outRect.left = space
                    outRect.right = space / 2
                } else if (params.spanIndex % 3 == 1) {
                    outRect.left = space / 2
                    outRect.right = space / 2
                } else if (params.spanIndex % 3 == 2) {
                    outRect.left = space / 2
                    outRect.right = space
                }
            } else if (Shaft.sSettings.lineCount == 4) {
                if (position == 0 || position == 1 || position == 2 || position == 3) {
                    outRect.top = space
                }

                if (params.spanIndex % 4 == 0) {
                    outRect.left = space
                    outRect.right = space / 2
                } else if (params.spanIndex % 4 == 1 || params.spanIndex % 4 == 2) {
                    outRect.left = space / 2
                    outRect.right = space / 2
                } else if (params.spanIndex % 4 == 3) {
                    outRect.left = space / 2
                    outRect.right = space
                }
            }
        }
    }
}
