package ceui.lisa.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import ceui.lisa.R
import ceui.lisa.utils.Common

class UserHeaderBehavior : CoordinatorLayout.Behavior<View> {
    private var headerHeight = 0f
    private var toolbarHeight = 0
    private lateinit var centerView: View
    private lateinit var toolbarTitleView: View

    constructor()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int,
    ): Boolean {
        val toolbar = parent.findViewById<View>(R.id.toolbar)
        toolbarHeight = toolbar.measuredHeight
        headerHeight =
            parent.findViewById<View>(R.id.imagesTitleBlockLayout).measuredHeight.toFloat() - toolbarHeight
        toolbarTitleView = toolbar.findViewById(R.id.toolbar_title)
        centerView = parent.findViewById(R.id.center_header)
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View,
    ): Boolean {
        return dependency.id == R.id.content_item
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View,
    ): Boolean {
        Common.showLog("onDependentViewChanged ${child.translationY}")
        toolbarTitleView.alpha = -(child.translationY / headerHeight)
        centerView.alpha = 1 - child.translationY / -headerHeight

        if (kotlin.math.abs(child.translationY) < 10) {
            toolbarTitleView.alpha = 0.0f
            centerView.alpha = 1.0f
        }

        child.translationY = dependency.translationY
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
        val result = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        Common.showLog("onStartNestedScroll $result")
        return result
    }
}
