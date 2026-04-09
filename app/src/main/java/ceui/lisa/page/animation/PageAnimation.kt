package ceui.lisa.page.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller

abstract class PageAnimation(
    w: Int,
    h: Int,
    marginWidth: Int,
    marginHeight: Int,
    view: View,
    listener: OnPageChangeListener,
) {
    @JvmField
    protected var mView: View?

    @JvmField
    protected var mListener: OnPageChangeListener

    @JvmField
    var autoPageIsRunning = false

    @JvmField
    var isCancelTouch = false

    @JvmField
    protected var mScroller: Scroller

    @JvmField
    protected var mDirection: Direction = Direction.NONE

    @JvmField
    protected var isRunning = false

    @JvmField
    protected var mScreenWidth: Int = w

    @JvmField
    protected var mScreenHeight: Int = h

    @JvmField
    protected var mMarginWidth: Int = marginWidth

    @JvmField
    protected var mMarginHeight: Int = marginHeight

    @JvmField
    protected var mViewWidth: Int = mScreenWidth - mMarginWidth * 2

    @JvmField
    protected var mViewHeight: Int = mScreenHeight - mMarginHeight * 2

    @JvmField
    protected var mStartX = 0f

    @JvmField
    protected var mStartY = 0f

    @JvmField
    protected var mTouchX = 0f

    @JvmField
    protected var mTouchY = 0f

    @JvmField
    protected var mLastX = 0f

    @JvmField
    protected var mLastY = 0f

    init {
        mView = view
        mListener = listener
        mScroller = Scroller(mView!!.context, LinearInterpolator())
    }

    constructor(w: Int, h: Int, view: View, listener: OnPageChangeListener) : this(
        w,
        h,
        0,
        0,
        view,
        listener,
    )

    open fun setStartPoint(x: Float, y: Float) {
        mStartX = x
        mStartY = y

        mLastX = mStartX
        mLastY = mStartY
    }

    open fun setTouchPoint(x: Float, y: Float) {
        mLastX = mTouchX
        mLastY = mTouchY

        mTouchX = x
        mTouchY = y
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    open fun startAnim() {
        if (isRunning || autoPageIsRunning) {
            return
        }
        isRunning = true
    }

    fun getDirection(): Direction {
        return mDirection
    }

    open fun setDirection(direction: Direction) {
        mDirection = direction
    }

    fun clear() {
        mView = null
    }

    abstract fun onTouchEvent(event: MotionEvent): Boolean

    abstract fun draw(canvas: Canvas)

    abstract fun scrollAnim()

    abstract fun abortAnim()

    abstract fun getBgBitmap(): Bitmap

    abstract fun getNextBitmap(): Bitmap

    enum class Direction(
        @JvmField val isHorizontal: Boolean,
    ) {
        NONE(true),
        NEXT(true),
        PRE(true),
        UP(false),
        DOWN(false),
    }

    interface OnPageChangeListener {
        fun hasPrev(execute: Boolean): Boolean

        fun hasNext(execute: Boolean): Boolean

        fun pageCancel()

        fun turnPage()
    }
}
