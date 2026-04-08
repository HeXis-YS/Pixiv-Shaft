package ceui.lisa.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import ceui.lisa.R

class FragmentRightContentBehavior : CoordinatorLayout.Behavior<View> {
    private var headerHeight = 0f
    private lateinit var contentView: View
    private var scroller: OverScroller? = null
    private val scrollRunnable =
        object : Runnable {
            override fun run() {
                scroller?.let { currentScroller ->
                    if (currentScroller.computeScrollOffset()) {
                        contentView.translationY = currentScroller.currY.toFloat()
                        ViewCompat.postOnAnimation(contentView, this)
                    }
                }
            }
        }

    constructor()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private fun startAutoScroll(current: Int, target: Int, duration: Int) {
        if (scroller == null) {
            scroller = OverScroller(contentView.context)
        }
        val currentScroller = scroller ?: return
        if (currentScroller.isFinished) {
            contentView.removeCallbacks(scrollRunnable)
            currentScroller.startScroll(0, current, 0, target - current, duration)
            ViewCompat.postOnAnimation(contentView, scrollRunnable)
        }
    }

    private fun stopAutoScroll() {
        scroller?.let { currentScroller ->
            if (!currentScroller.isFinished) {
                currentScroller.abortAnimation()
                contentView.removeCallbacks(scrollRunnable)
            }
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int,
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (child.translationY >= 0f || child.translationY <= -headerHeight) {
            return
        }
        if (child.translationY <= -headerHeight * 0.5f) {
            stopAutoScroll()
            startAutoScroll(child.translationY.toInt(), -headerHeight.toInt(), 1000)
        } else {
            stopAutoScroll()
            startAutoScroll(child.translationY.toInt(), 0, 600)
        }
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View,
    ): Boolean {
        return child.id == R.id.imagesTitleBlockLayout
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int,
    ): Boolean {
        parent.onLayoutChild(child, layoutDirection)
        headerHeight = parent.findViewById<View>(R.id.imagesTitleBlockLayout).measuredHeight.toFloat()
        contentView = child
        ViewCompat.offsetTopAndBottom(child, headerHeight.toInt())
        return true
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int,
    ): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int,
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        stopAutoScroll()
        if (dy > 0) {
            val newTransY = child.translationY - dy
            if (newTransY >= -headerHeight) {
                consumed[1] = dy
                child.translationY = newTransY
            } else {
                consumed[1] = (headerHeight + child.translationY).toInt()
                child.translationY = -headerHeight
            }
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray,
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed,
        )
        stopAutoScroll()
        if (dyUnconsumed < 0) {
            val newTransY = child.translationY - dyUnconsumed
            child.translationY = if (newTransY <= 0) newTransY else 0f
        }
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View,
    ): Boolean {
        return super.onDependentViewChanged(parent, child, dependency)
    }
}
