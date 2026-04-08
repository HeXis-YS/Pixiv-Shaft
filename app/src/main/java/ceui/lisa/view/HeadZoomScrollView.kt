package ceui.lisa.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView

class HeadZoomScrollView : NestedScrollView {
    private var y = 0f
    private var zoomViewWidth = 0
    private var zoomViewHeight = 0
    private var mScaling = false
    private var zoomView: View? = null
    private var mScaleRatio = 0.4f
    private var mScaleTimes = 2f
    private var mReplyRatio = 0.5f
    private var onScrollListener: OnScrollListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setZoomView(zoomView: View?) {
        this.zoomView = zoomView
    }

    fun setmScaleRatio(mScaleRatio: Float) {
        this.mScaleRatio = mScaleRatio
    }

    fun setmScaleTimes(mScaleTimes: Int) {
        this.mScaleTimes = mScaleTimes.toFloat()
    }

    fun setmReplyRatio(mReplyRatio: Float) {
        this.mReplyRatio = mReplyRatio
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        overScrollMode = OVER_SCROLL_NEVER
        if (getChildAt(0) is ViewGroup && zoomView == null) {
            val vg = getChildAt(0) as ViewGroup
            if (vg.childCount > 0) {
                zoomView = vg.getChildAt(0)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val currentZoomView = zoomView
        if (zoomViewWidth <= 0 || zoomViewHeight <= 0) {
            zoomViewWidth = currentZoomView?.measuredWidth ?: 0
            zoomViewHeight = currentZoomView?.measuredHeight ?: 0
        }
        if (currentZoomView == null || zoomViewWidth <= 0 || zoomViewHeight <= 0) {
            return super.onTouchEvent(ev)
        }
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                if (!mScaling) {
                    if (scrollY == 0) {
                        y = ev.y
                    } else {
                        return super.onTouchEvent(ev)
                    }
                }
                val distance = ((ev.y - y) * mScaleRatio).toInt()
                if (distance < 0) {
                    return super.onTouchEvent(ev)
                }
                mScaling = true
                setZoom(distance.toFloat())
                return true
            }

            MotionEvent.ACTION_UP -> {
                mScaling = false
                replyView()
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun setZoom(s: Float) {
        val currentZoomView = zoomView ?: return
        val scaleTimes = (zoomViewWidth + s) / zoomViewWidth
        if (scaleTimes > mScaleTimes) return

        val layoutParams = currentZoomView.layoutParams
        layoutParams.width = (zoomViewWidth + s).toInt()
        layoutParams.height = (zoomViewHeight * ((zoomViewWidth + s) / zoomViewWidth)).toInt()
        (layoutParams as MarginLayoutParams).setMargins(-(layoutParams.width - zoomViewWidth) / 2, 0, 0, 0)
        currentZoomView.layoutParams = layoutParams
    }

    private fun replyView() {
        val currentZoomView = zoomView ?: return
        val distance = currentZoomView.measuredWidth - zoomViewWidth
        val anim: ValueAnimator = ObjectAnimator.ofFloat(distance.toFloat(), 0.0F).setDuration((distance * mReplyRatio).toLong())
        anim.addUpdateListener { animation ->
            setZoom(animation.animatedValue as Float)
        }
        anim.start()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollListener?.onScroll(l, t, oldl, oldt)
    }

    fun setOnScrollListener(onScrollListener: OnScrollListener?) {
        this.onScrollListener = onScrollListener
    }

    interface OnScrollListener {
        fun onScroll(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)
    }
}
