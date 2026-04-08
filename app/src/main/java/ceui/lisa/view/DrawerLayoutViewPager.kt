package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import kotlin.math.abs

class DrawerLayoutViewPager : ViewPager {

    private var startX = 0f
    private var startY = 0f
    private val leftThreshold = ScreenUtils.getScreenWidth() * 0.1f

    private var touchEventForwarder: IForwardTouchEvent? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setTouchEventForwarder(touchEventForwarder: IForwardTouchEvent?) {
        this.touchEventForwarder = touchEventForwarder
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            startX = ev.x
            startY = ev.y
            parent.requestDisallowInterceptTouchEvent(startX >= leftThreshold)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x
                val endY = ev.y
                val distanceX = endX - startX
                val distanceY = endY - startY
                if (abs(distanceX) > abs(distanceY)) {
                    if (currentItem == 0 && distanceX > 0) {
                        parent.requestDisallowInterceptTouchEvent(false)
                        touchEventForwarder?.forwardTouchEvent(ev)
                        return true
                    }
                    if (currentItem != 0 && distanceX > 0 && startX < leftThreshold) {
                        touchEventForwarder?.forwardTouchEvent(ev)
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> Unit
        }
        return super.onTouchEvent(ev)
    }

    fun interface IForwardTouchEvent {
        fun forwardTouchEvent(ev: MotionEvent)
    }
}
