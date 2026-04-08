package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class FixedViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
            false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
            false
        }
    }
}
