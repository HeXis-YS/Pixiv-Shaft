package ceui.lisa.page.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.Iterator

class ScrollPageAnim(
    w: Int,
    h: Int,
    marginWidth: Int,
    marginHeight: Int,
    view: View,
    listener: OnPageChangeListener,
) : PageAnimation(w, h, marginWidth, marginHeight, view, listener) {

    private var tmpView: BitmapView? = null
    private var mVelocity: VelocityTracker? = null
    private var mBgBitmap: Bitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565)
    private var mNextBitmap: Bitmap? = null
    private var mScrapViews: ArrayDeque<BitmapView> = ArrayDeque(2)
    private val mActiveViews = ArrayList<BitmapView>(2)
    private var isRefresh = true
    private var downIt: MutableIterator<BitmapView>? = null
    private var upIt: MutableIterator<BitmapView>? = null

    init {
        initWidget()
    }

    private fun initWidget() {
        mBgBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565)

        mScrapViews = ArrayDeque(2)
        for (i in 0..1) {
            val view = BitmapView()
            view.bitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565)
            view.srcRect = Rect(0, 0, mViewWidth, mViewHeight)
            view.destRect = Rect(0, 0, mViewWidth, mViewHeight)
            view.top = 0
            view.bottom = view.bitmap.height

            mScrapViews.push(view)
        }
        onLayout()
        isRefresh = false
    }

    private fun onLayout() {
        if (mActiveViews.size == 0) {
            fillDown(0, 0)
            mDirection = Direction.NONE
        } else {
            val offset = (mTouchY - mLastY).toInt()
            if (offset > 0) {
                val topEdge = mActiveViews[0].top
                fillUp(topEdge, offset)
            } else {
                val bottomEdge = mActiveViews[mActiveViews.size - 1].bottom
                fillDown(bottomEdge, offset)
            }
        }
    }

    private fun fillDown(bottomEdge: Int, offset: Int) {
        downIt = mActiveViews.iterator()
        var view: BitmapView

        while (downIt!!.hasNext()) {
            view = downIt!!.next()
            view.top += offset
            view.bottom += offset
            view.destRect.top = view.top
            view.destRect.bottom = view.bottom

            if (view.bottom <= 0) {
                mScrapViews.add(view)
                downIt!!.remove()
                if (mDirection == Direction.UP) {
                    mListener.pageCancel()
                    mDirection = Direction.NONE
                }
            }
        }

        var realEdge = bottomEdge + offset

        while (realEdge < mViewHeight && mActiveViews.size < 2) {
            view = mScrapViews.first
            val cancelBitmap = mNextBitmap
            mNextBitmap = view.bitmap

            if (!isRefresh) {
                val hasNext = mListener.hasNext(true)
                if (!hasNext) {
                    mNextBitmap = cancelBitmap
                    for (activeView in mActiveViews) {
                        activeView.top = 0
                        activeView.bottom = mViewHeight
                        activeView.destRect.top = activeView.top
                        activeView.destRect.bottom = activeView.bottom
                    }
                    abortAnim()
                    return
                }
            }

            mScrapViews.removeFirst()
            mActiveViews.add(view)
            mDirection = Direction.DOWN

            view.top = realEdge
            view.bottom = realEdge + view.bitmap.height
            view.destRect.top = view.top
            view.destRect.bottom = view.bottom

            realEdge += view.bitmap.height
        }
    }

    private fun fillUp(topEdge: Int, offset: Int) {
        upIt = mActiveViews.iterator()
        var view: BitmapView
        while (upIt!!.hasNext()) {
            view = upIt!!.next()
            view.top += offset
            view.bottom += offset
            view.destRect.top = view.top
            view.destRect.bottom = view.bottom

            if (view.top >= mViewHeight) {
                mScrapViews.add(view)
                upIt!!.remove()

                if (mDirection == Direction.DOWN) {
                    mListener.pageCancel()
                    mDirection = Direction.NONE
                }
            }
        }

        var realEdge = topEdge + offset

        while (realEdge > 0 && mActiveViews.size < 2) {
            view = mScrapViews.first

            val cancelBitmap = mNextBitmap
            mNextBitmap = view.bitmap
            if (!isRefresh) {
                val hasPrev = mListener.hasPrev(true)
                if (!hasPrev) {
                    mNextBitmap = cancelBitmap
                    for (activeView in mActiveViews) {
                        activeView.top = 0
                        activeView.bottom = mViewHeight
                        activeView.destRect.top = activeView.top
                        activeView.destRect.bottom = activeView.bottom
                    }
                    abortAnim()
                    return
                }
            }

            mScrapViews.removeFirst()
            mActiveViews.add(0, view)
            mDirection = Direction.UP
            view.top = realEdge - view.bitmap.height
            view.bottom = realEdge
            view.destRect.top = view.top
            view.destRect.bottom = view.bottom
            realEdge -= view.bitmap.height
        }
    }

    private fun eraseBitmap(
        b: Bitmap,
        width: Int,
        height: Int,
        paddingLeft: Int,
        paddingTop: Int,
    ) {
    }

    fun resetBitmap() {
        isRefresh = true
        for (view in mActiveViews) {
            mScrapViews.add(view)
        }
        mActiveViews.clear()
        onLayout()
        isRefresh = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        if (mVelocity == null) {
            mVelocity = VelocityTracker.obtain()
        }

        mVelocity!!.addMovement(event)
        setTouchPoint(x.toFloat(), y.toFloat())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isRunning = false
                setStartPoint(x.toFloat(), y.toFloat())
                abortAnim()
            }

            MotionEvent.ACTION_MOVE -> {
                mVelocity!!.computeCurrentVelocity(VELOCITY_DURATION.toInt())
                isRunning = true
                mView!!.postInvalidate()
            }

            MotionEvent.ACTION_UP -> {
                isRunning = false
                startAnim()
                mVelocity!!.recycle()
                mVelocity = null
            }

            MotionEvent.ACTION_CANCEL -> {
                try {
                    mVelocity!!.recycle()
                    mVelocity = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    override fun draw(canvas: Canvas) {
        onLayout()

        canvas.drawBitmap(mBgBitmap, 0f, 0f, null)
        canvas.save()
        canvas.translate(0f, mMarginHeight.toFloat())
        canvas.clipRect(0, 0, mViewWidth, mViewHeight)
        for (i in mActiveViews.indices) {
            tmpView = mActiveViews[i]
            canvas.drawBitmap(tmpView!!.bitmap, tmpView!!.srcRect, tmpView!!.destRect, null)
        }
        canvas.restore()
    }

    @Synchronized
    override fun startAnim() {
        isRunning = true
        mScroller.fling(
            0,
            mTouchY.toInt(),
            0,
            mVelocity!!.yVelocity.toInt(),
            0,
            0,
            Int.MAX_VALUE * -1,
            Int.MAX_VALUE,
        )
    }

    override fun scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            val x = mScroller.currX
            val y = mScroller.currY
            setTouchPoint(x.toFloat(), y.toFloat())
            if (mScroller.finalX == x && mScroller.finalY == y) {
                isRunning = false
                autoPageIsRunning = false
            }
            mView!!.postInvalidate()
        }
    }

    override fun abortAnim() {
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
            isRunning = false
            autoPageIsRunning = false
        }
    }

    override fun getBgBitmap(): Bitmap {
        return mBgBitmap
    }

    override fun getNextBitmap(): Bitmap {
        return mNextBitmap!!
    }

    private class BitmapView {
        lateinit var bitmap: Bitmap
        lateinit var srcRect: Rect
        lateinit var destRect: Rect
        var top = 0
        var bottom = 0
    }

    companion object {
        private const val VELOCITY_DURATION = 1000
    }
}
