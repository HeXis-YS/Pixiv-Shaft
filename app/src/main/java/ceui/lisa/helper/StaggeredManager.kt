package ceui.lisa.helper

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlin.math.sqrt

class StaggeredManager : StaggeredGridLayoutManager {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes,
    )

    constructor(spanCount: Int, orientation: Int) : super(spanCount, orientation)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val scroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
                try {
                    if (!targetView.getGlobalVisibleRect(Rect())) {
                        val rect = Rect()
                        recyclerView.getGlobalVisibleRect(rect)

                        val parentHeight = rect.bottom - rect.top
                        val childHeight = targetView.height
                        val offset = (parentHeight - childHeight) / 2

                        val dx = calculateDxToMakeVisible(targetView, horizontalSnapPreference)
                        val dy = calculateDyToMakeVisible(targetView, verticalSnapPreference) + offset
                        val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
                        val time = calculateTimeForDeceleration(distance)
                        if (time > 0) {
                            action.update(-dx, -dy, time, mDecelerateInterpolator)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 40f / displayMetrics.densityDpi
            }
        }
        scroller.targetPosition = position
        startSmoothScroll(scroller)
    }
}
