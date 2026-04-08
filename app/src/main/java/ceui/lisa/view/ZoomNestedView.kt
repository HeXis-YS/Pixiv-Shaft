package ceui.lisa.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.widget.NestedScrollView

class ZoomNestedView : NestedScrollView {
    private var mZoomView: View? = null
    private var mZoomViewWidth = 0
    private var mZoomViewHeight = 0

    private var firstPosition = 0f
    private var isScrolling = false
    private var mScrollRate = 0.3f
    private var mReplyRate = 0.5f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setmZoomView(mZoomView: View?) {
        this.mZoomView = mZoomView
    }

    fun setmScrollRate(mScrollRate: Float) {
        this.mScrollRate = mScrollRate
    }

    fun setmReplyRate(mReplyRate: Float) {
        this.mReplyRate = mReplyRate
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        init()
    }

    private fun init() {
        overScrollMode = OVER_SCROLL_NEVER
        val child = getChildAt(0)
        if (child is ViewGroup) {
            val zoomView = child.getChildAt(0)
            if (zoomView != null) {
                mZoomView = zoomView
                zoomView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        mZoomViewWidth = zoomView.measuredWidth
                        mZoomViewHeight = zoomView.measuredHeight
                        zoomView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP -> {
                isScrolling = false
                replyImage()
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isScrolling) {
                    if (scrollY == 0) {
                        firstPosition = ev.y
                    } else {
                        return super.onTouchEvent(ev)
                    }
                }
                val distance = ((ev.y - firstPosition) * mScrollRate).toInt()
                if (distance < 0) {
                    return super.onTouchEvent(ev)
                }
                isScrolling = true
                setZoom(distance.toFloat())
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun replyImage() {
        val zoomView = mZoomView ?: return
        val distance = zoomView.measuredWidth - mZoomViewWidth
        val valueAnimator = ValueAnimator.ofFloat(distance.toFloat(), 0f).setDuration((distance * mReplyRate).toLong())
        valueAnimator.addUpdateListener { animation ->
            setZoom(animation.animatedValue as Float)
        }
        valueAnimator.start()
    }

    fun setZoom(zoom: Float) {
        val zoomView = mZoomView ?: return
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            return
        }
        val lp = zoomView.layoutParams
        lp.width = (mZoomViewWidth + zoom).toInt()
        lp.height = (mZoomViewHeight * ((mZoomViewWidth + zoom) / mZoomViewWidth)).toInt()
        (lp as MarginLayoutParams).setMargins(-(lp.width - mZoomViewWidth) / 2, 0, -(lp.width - mZoomViewWidth) / 2, 0)
        zoomView.layoutParams = lp
    }
}
