package ceui.lisa.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TagItemDecoration2(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        if (position == 0) {
            outRect.left = spacing
            outRect.top = spacing
            outRect.bottom = spacing
            outRect.right = spacing / 2
        } else if (position == 1) {
            outRect.right = spacing
            outRect.top = spacing
            outRect.bottom = spacing
            outRect.left = spacing / 2
        } else {
            if (position % 2 == 0) {
                outRect.left = spacing
                outRect.bottom = spacing
                outRect.right = spacing / 2
            } else {
                outRect.left = spacing / 2
                outRect.bottom = spacing
                outRect.right = spacing
            }
        }
    }
}
