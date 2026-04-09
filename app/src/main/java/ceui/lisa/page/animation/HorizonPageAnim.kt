package ceui.lisa.page.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

abstract class HorizonPageAnim(
    w: Int,
    h: Int,
    marginWidth: Int,
    marginHeight: Int,
    view: View,
    listener: OnPageChangeListener,
) : PageAnimation(w, h, marginWidth, marginHeight, view, listener) {

    @JvmField
    protected var mCurBitmap: Bitmap =
        Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565)

    @JvmField
    protected var mNextBitmap: Bitmap =
        Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565)

    @JvmField
    protected var isCancel = false

    @JvmField
    protected var executePreOrNextPage = false

    private var x = 0
    private var y = 0
    private var mMoveX = 0
    private var mMoveY = 0
    private var isMove = false
    private var isNext = false
    private var noNext = false

    constructor(w: Int, h: Int, view: View, listener: OnPageChangeListener) : this(
        w,
        h,
        0,
        0,
        view,
        listener,
    )

    fun changePage() {
        val bitmap = mCurBitmap
        mCurBitmap = mNextBitmap
        mNextBitmap = bitmap
    }

    abstract fun drawStatic(canvas: Canvas)

    abstract fun drawMove(canvas: Canvas)

    private fun simulationTouchDown(x: Int, y: Int) {
        mMoveX = 0
        mMoveY = 0
        isMove = false
        noNext = false
        isNext = false
        isRunning = false
        isCancel = false
        executePreOrNextPage = true
        setStartPoint(x.toFloat(), y.toFloat())
        abortAnim()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        x = event.x.toInt()
        y = event.y.toInt()
        setTouchPoint(x.toFloat(), y.toFloat())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> simulationTouchDown(x, y)

            MotionEvent.ACTION_MOVE -> {
                val slop = ViewConfiguration.get(mView!!.context).scaledTouchSlop
                if (!isMove) {
                    isMove = kotlin.math.abs(mStartX - x) > slop || kotlin.math.abs(mStartY - y) > slop
                }

                if (isMove) {
                    if (mMoveX == 0 && mMoveY == 0) {
                        if (x - mStartX > 0) {
                            isNext = false
                            val hasPrev = mListener.hasPrev(executePreOrNextPage)
                            executePreOrNextPage = false
                            setDirection(Direction.PRE)
                            if (!hasPrev) {
                                noNext = true
                                return true
                            }
                        } else {
                            isNext = true
                            val hasNext = mListener.hasNext(executePreOrNextPage)
                            executePreOrNextPage = false
                            setDirection(Direction.NEXT)
                            if (!hasNext) {
                                noNext = true
                                return true
                            }
                        }
                    } else {
                        isCancel =
                            if (isNext) {
                                x - mMoveX > 0
                            } else {
                                x - mMoveX < 0
                            }
                    }

                    mMoveX = x
                    mMoveY = y
                    isRunning = true

                    if (this !is NonePageAnim) {
                        mView!!.invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!isMove) {
                    isNext = x >= mScreenWidth / 2
                    if (isNext) {
                        val hasNext = mListener.hasNext(true)
                        setDirection(Direction.NEXT)
                        if (!hasNext) {
                            return true
                        }
                    } else {
                        val hasPrev = mListener.hasPrev(true)
                        setDirection(Direction.PRE)
                        if (!hasPrev) {
                            return true
                        }
                    }
                }

                if (isCancel) {
                    if (this !is NonePageAnim) {
                        mListener.pageCancel()
                    }
                } else {
                    mListener.turnPage()
                }

                if (!noNext) {
                    startAnim()
                    mView!!.invalidate()
                }
            }
        }
        return true
    }

    override fun draw(canvas: Canvas) {
        if (isRunning) {
            drawMove(canvas)
        } else {
            if (isCancel) {
                mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true)
            }
            drawStatic(canvas)
        }
    }

    override fun scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            val x = mScroller.currX
            val y = mScroller.currY

            setTouchPoint(x.toFloat(), y.toFloat())

            if (mScroller.finalX == x && mScroller.finalY == y) {
                isRunning = false
                if (autoPageIsRunning) {
                    isCancel = true
                    simulationTouchDown(0, 0)
                    setTouchPoint(mScroller.finalX.toFloat(), mScroller.finalY.toFloat())
                    autoPageIsRunning = false
                }
            }
            mView!!.postInvalidate()
        }
    }

    override fun abortAnim() {
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
            isRunning = false
            autoPageIsRunning = false
            setTouchPoint(mScroller.finalX.toFloat(), mScroller.finalY.toFloat())
            mView!!.postInvalidate()
        }
    }

    override fun getBgBitmap(): Bitmap {
        return mNextBitmap
    }

    override fun getNextBitmap(): Bitmap {
        return mNextBitmap
    }
}
