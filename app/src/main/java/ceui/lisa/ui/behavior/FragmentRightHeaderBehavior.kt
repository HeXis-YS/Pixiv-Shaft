package ceui.lisa.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import ceui.lisa.R
import ceui.lisa.utils.Common

class FragmentRightHeaderBehavior : CoordinatorLayout.Behavior<View> {

    constructor()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int,
    ): Boolean {
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
        child.translationY = dependency.translationY * 0.6f
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
