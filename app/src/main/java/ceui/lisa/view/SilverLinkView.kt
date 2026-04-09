package ceui.lisa.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import ceui.lisa.utils.Common
import java.util.HashMap

class SilverLinkView : View, GestureDetector.OnGestureListener {
    private var radius = 0f
    private var circles = 0
    private lateinit var mContext: Context
    private var centerX = 0f
    private var centerY = 0f
    private val mHandler =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler(Looper.getMainLooper())
        } else {
            @Suppress("DEPRECATION")
            Handler()
        }
    private val mHashMap = HashMap<Int, Int>()
    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mPaint: Paint
    private val colors =
        intArrayOf(
            0xFFd50000.toInt(),
            0xFFc51162.toInt(),
            0xFFaa00ff.toInt(),
            0xFF6200ea.toInt(),
            0xFF304ffe.toInt(),
            0xFF2962ff.toInt(),
            0xFF0091ea.toInt(),
            0xFF00b8d4.toInt(),
            0xFF00bfa5.toInt(),
            0xFF00c853.toInt(),
            0xFF64dd17.toInt(),
            0xFFaeea00.toInt(),
            0xFFffd600.toInt(),
            0xFFdd2c00.toInt(),
            0xFFe8eaf6.toInt(),
            0xFFc5cae9.toInt(),
            0xFF9fa8da.toInt(),
            0xFF7986cb.toInt(),
            0xFF5c6bc0.toInt(),
            0xFF3f51b5.toInt(),
            0xFF3949ab.toInt(),
            0xFF303f9f.toInt(),
            0xFF283593.toInt(),
            0xFF1a237e.toInt(),
            0xFF8c9eff.toInt(),
            0xFF536dfe.toInt(),
            0xFF3d5afe.toInt(),
            0xFF304ffe.toInt(),
            0xFFb39ddb.toInt(),
            0xFF9575cd.toInt(),
            0xFF7e57c2.toInt(),
            0xFF673ab7.toInt(),
            0xFF5e35b1.toInt(),
            0xFF512da8.toInt(),
            0xFF4527a0.toInt(),
            0xFF311b92.toInt(),
        )

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
    ) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        mGestureDetector = GestureDetector(mContext, this)
        mPaint = Paint()
        mPaint.color = colors[0]
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 8f
        mPaint.isAntiAlias = true
        mHashMap[0] = colors[0]
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        centerX = event.x
        centerY = event.y
        return if (event.action == MotionEvent.ACTION_UP) {
            end()
            true
        } else {
            mGestureDetector.onTouchEvent(event)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sq =
            Math.sqrt(
                (measuredWidth * measuredWidth + measuredHeight * measuredHeight).toDouble(),
            )

        var lastColor = 0
        for (i in 0 until circles) {
            val r =
                if (circles == 1) {
                    radius
                } else {
                    radius - i * size
                }

            if (r < sq) {
                val color =
                    if (mHashMap[i] == null) {
                        var nextColor: Int
                        do {
                            nextColor = colors[Common.flatRandom(0, colors.size)]
                        } while (nextColor == lastColor)
                        mHashMap[i] = nextColor
                        nextColor
                    } else {
                        mHashMap[i]!!
                    }

                mPaint.color = color
                lastColor = color
                canvas.drawCircle(centerX, centerY, r, mPaint)
            }
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        Common.showLog("SilverLinkView onDown ")
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        start()
        Common.showLog("SilverLinkView onShowPress ")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Common.showLog("SilverLinkView onSingleTapUp ")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        Common.showLog("SilverLinkView onScroll ")
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        Common.showLog("SilverLinkView onLongPress ")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {
        Common.showLog("SilverLinkView onFling ")
        return false
    }

    private fun start() {
        mRunnable.run()
    }

    private fun end() {
        mHandler.removeCallbacks(mRunnable)
    }

    private val mRunnable =
        object : Runnable {
            override fun run() {
                Common.showLog("SilverLinkView Runnable")
                radius += step
                step += 0.1f
                val ratio = radius / size
                circles =
                    if (ratio < 1) {
                        1
                    } else {
                        ratio.toInt()
                    }
                invalidate()
                mHandler.postDelayed(this, 16)
            }
        }

    companion object {
        private const val size = 200
    }

    private var step = 2.0f
}
