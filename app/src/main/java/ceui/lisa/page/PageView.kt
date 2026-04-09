package ceui.lisa.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import ceui.lisa.models.NovelDetail
import ceui.lisa.page.animation.HorizonPageAnim
import ceui.lisa.page.animation.PageAnimation
import ceui.lisa.page.animation.ScrollPageAnim
import ceui.lisa.page.animation.SimulationPageAnim
import ceui.lisa.utils.Common

class PageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    @JvmField
    var mPageAnim: PageAnimation? = null

    private var mViewWidth = 0
    private var mViewHeight = 0
    private var mStartX = 0
    private var mStartY = 0
    private var isMove = false
    private var mBgColor = 0xFFCEC29C.toInt()
    private var mPageMode = PageMode.SIMULATION
    private var canTouch = true
    private var mCenterRect: RectF? = null
    private var prepared = false
    private var mTouchListener: TouchListener? = null
    private var mPageLoader: PageLoader? = null
    private val mPageAnimListener =
        object : PageAnimation.OnPageChangeListener {
            override fun hasPrev(execute: Boolean): Boolean {
                val touchListener = mTouchListener ?: return false
                return if (!touchListener.allowPrePage()) {
                    false
                } else {
                    mPageLoader!!.prePage(execute)
                }
            }

            override fun hasNext(execute: Boolean): Boolean {
                val touchListener = mTouchListener ?: return false
                return if (!touchListener.allowNextPage()) {
                    false
                } else {
                    mPageLoader!!.nextPage(execute)
                }
            }

            override fun pageCancel() {
                mPageLoader!!.pageCancel()
            }

            override fun turnPage() {
                mPageLoader!!.turnPage()
            }
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        prepared = true

        if (mPageLoader != null) {
            mPageLoader!!.pageViewInitSuccess(mViewWidth, mViewHeight)
        }
    }

    fun setPageMode(pageMode: PageMode) {
        mPageMode = pageMode
        if (mViewWidth == 0 || mViewHeight == 0) {
            return
        }
        mPageAnim = SimulationPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener)
    }

    fun getNextBitmap(): Bitmap? {
        return mPageAnim?.getNextBitmap()
    }

    fun getBgBitmap(): Bitmap? {
        return mPageAnim?.getBgBitmap()
    }

    fun autoPrevPage(): Boolean {
        return if (mPageAnim is ScrollPageAnim) {
            false
        } else {
            startPageAnim(PageAnimation.Direction.PRE)
            true
        }
    }

    fun autoNextPage(): Boolean {
        return if (mPageAnim is ScrollPageAnim) {
            false
        } else {
            startPageAnim(PageAnimation.Direction.NEXT)
            true
        }
    }

    private fun startPageAnim(direction: PageAnimation.Direction) {
        val touchListener = mTouchListener ?: return
        val pageAnim = mPageAnim ?: return
        val pageLoader = mPageLoader ?: return

        pageAnim.autoPageIsRunning = true

        abortAnimation()
        if (direction == PageAnimation.Direction.NEXT) {
            val x = mViewWidth
            val y = mViewHeight
            pageAnim.setStartPoint(x.toFloat(), y.toFloat())
            pageAnim.setTouchPoint(x.toFloat(), y.toFloat())
            pageAnim.setDirection(direction)

            if (touchListener.allowNextPage()) {
                pageLoader.nextPage(true)
            }
        } else {
            val x = 0
            val y = mViewHeight
            pageAnim.setStartPoint(x.toFloat(), y.toFloat())
            pageAnim.setTouchPoint(x.toFloat(), y.toFloat())
            pageAnim.setDirection(direction)

            if (touchListener.allowPrePage()) {
                pageLoader.prePage(true)
            }
        }
        pageAnim.startAnim()
        postInvalidate()
    }

    fun setBgColor(color: Int) {
        mBgColor = color
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(mBgColor)
        mPageAnim?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pageLoader = mPageLoader ?: return true
        if (pageLoader.isRequesting()) {
            return true
        }

        super.onTouchEvent(event)

        if (!canTouch && event.action != MotionEvent.ACTION_DOWN) {
            return true
        }

        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                isMove = false
                canTouch = mTouchListener!!.onTouch()
                mPageAnim!!.onTouchEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                val slop = ViewConfiguration.get(context).scaledTouchSlop
                if (!isMove) {
                    isMove =
                        kotlin.math.abs(mStartX - event.x) > slop ||
                            kotlin.math.abs(mStartY - event.y) > slop
                }

                if (isMove) {
                    mPageAnim!!.onTouchEvent(event)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!isMove) {
                    if (mCenterRect == null) {
                        mCenterRect =
                            RectF(
                                (mViewWidth / 5).toFloat(),
                                (mViewHeight / 3).toFloat(),
                                (mViewWidth * 4 / 5).toFloat(),
                                (mViewHeight * 2 / 3).toFloat(),
                            )
                    }

                    if (mCenterRect!!.contains(x.toFloat(), y.toFloat())) {
                        if (mTouchListener != null) {
                            mTouchListener!!.center()
                        }
                        return true
                    }
                }
                mPageAnim!!.onTouchEvent(event)
            }
        }
        return true
    }

    override fun computeScroll() {
        mPageAnim?.scrollAnim()
        super.computeScroll()
    }

    fun abortAnimation() {
        mPageAnim?.abortAnim()
    }

    fun isRunning(): Boolean {
        return mPageAnim?.isRunning() ?: false
    }

    fun isPrepare(): Boolean {
        return prepared
    }

    fun setTouchListener(mTouchListener: TouchListener) {
        this.mTouchListener = mTouchListener
    }

    fun drawNextPage() {
        if (!prepared) {
            return
        }

        val pageAnim = mPageAnim ?: return
        val pageLoader = mPageLoader ?: return
        if (pageAnim is HorizonPageAnim) {
            pageAnim.changePage()
        }
        pageLoader.drawPage(getNextBitmap(), false)
    }

    fun drawCurPage(isUpdate: Boolean) {
        if (!prepared) {
            return
        }

        val pageAnim = mPageAnim ?: return
        val pageLoader = mPageLoader ?: return
        if (!isUpdate && pageAnim is ScrollPageAnim) {
            pageAnim.resetBitmap()
        }
        Common.showLog("drawContent drawCurPage ")
        pageLoader.drawPage(getNextBitmap(), isUpdate)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (mPageAnim != null) {
            mPageAnim!!.abortAnim()
            mPageAnim!!.clear()
        }

        mPageLoader = null
        mPageAnim = null
    }

    fun getPageLoader(novelBean: NovelDetail?): PageLoader {
        if (mPageLoader != null) {
            return mPageLoader!!
        }

        mPageLoader = PageLoader(this, novelBean)
        return mPageLoader!!
    }

    interface TouchListener {
        fun onTouch(): Boolean

        fun center()

        fun allowPrePage(): Boolean

        fun prePage()

        fun allowNextPage(): Boolean

        fun nextPage()

        fun cancel()
    }
}
