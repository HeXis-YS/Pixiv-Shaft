package ceui.lisa.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Scroller
import ceui.lisa.utils.Common

class BottomSlideLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var mDetector: GestureDetector
    private lateinit var bottomBar: View
    private lateinit var bottomContent: View
    private lateinit var mScroller: Scroller
    private lateinit var barRect: Rect
    private var downX = 0
    private var downY = 0
    private var scrollOffset = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        initView()
    }

    private fun initView() {
        val count = childCount
        if (count != 2) {
            return
        }
        bottomBar = getChildAt(0)
        bottomContent = getChildAt(1)
        barRect = Rect()
        mScroller = Scroller(context)
        bottomBar.getGlobalVisibleRect(barRect)
        mDetector = GestureDetector(
            context,
            object : GestureDetector.OnGestureListener {
                override fun onDown(e: MotionEvent): Boolean {
                    return false
                }

                override fun onShowPress(e: MotionEvent) {
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return false
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean {
                    Common.showLog("BottomSlideLayout onLongPress")
                    return false
                }

                override fun onLongPress(e: MotionEvent) {
                    Common.showLog("BottomSlideLayout onLongPress")
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float,
                ): Boolean {
                    Common.showLog("BottomSlideLayout onFling")
                    return false
                }
            },
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt()
                downY = event.y.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                val endY = event.y.toInt()
                val dy = endY - downY
                var toScroll = scrollY - dy
                if (toScroll < 0) {
                    toScroll = 0
                } else if (toScroll > bottomContent.measuredHeight) {
                    toScroll = bottomContent.measuredHeight
                }
                scrollTo(0, toScroll)
                downY = event.y.toInt()
            }

            MotionEvent.ACTION_UP -> {
                scrollOffset = scrollY
                if (scrollOffset > bottomContent.measuredHeight / 2) {
                    showNavigation()
                } else {
                    closeNavigation()
                }
            }
        }
        return true
    }

    private fun showNavigation() {
        val dy = bottomContent.measuredHeight - scrollOffset
        mScroller.startScroll(scrollX, scrollY, 0, dy, 500)
        invalidate()
    }

    private fun closeNavigation() {
        val dy = 0 - scrollOffset
        mScroller.startScroll(scrollX, scrollY, 0, dy, 500)
        invalidate()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        bottomBar.layout(0, measuredHeight - bottomBar.measuredHeight, measuredWidth, measuredHeight)
        bottomContent.layout(
            0,
            measuredHeight,
            measuredWidth,
            bottomBar.bottom + bottomContent.measuredHeight,
        )
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            invalidate()
        }
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean {
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }
}
