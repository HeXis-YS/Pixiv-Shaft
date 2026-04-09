package ceui.lisa.page.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.widget.Scroller

abstract class AnimationProvider(width: Int, height: Int) {
    protected var mCurPageBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    protected var mNextPageBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    protected var myStartX = 0f
    protected var myStartY = 0f
    protected var myEndX = 0
    protected var myEndY = 0
    protected var myDirection: Direction? = null
    protected var mScreenWidth = width
    protected var mScreenHeight = height
    protected var mTouch: PointF = PointF()
    private var direction = Direction.NONE
    private var isCancel = false

    abstract fun drawMove(canvas: Canvas)

    abstract fun drawStatic(canvas: Canvas)

    fun setStartPoint(x: Float, y: Float) {
        myStartX = x
        myStartY = y
    }

    fun setTouchPoint(x: Float, y: Float) {
        mTouch.x = x
        mTouch.y = y
    }

    fun getDirection(): Direction = direction

    fun setDirection(direction: Direction) {
        this.direction = direction
    }

    abstract fun startAnimation(scroller: Scroller)

    fun changePage() {
        val bitmap = mCurPageBitmap
        mCurPageBitmap = mNextPageBitmap
        mNextPageBitmap = bitmap
    }

    fun getNextBitmap(): Bitmap = mNextPageBitmap

    fun getBgBitmap(): Bitmap = mNextPageBitmap

    fun getCancel(): Boolean = isCancel

    fun setCancel(isCancel: Boolean) {
        this.isCancel = isCancel
    }

    enum class Direction(
        @JvmField val isHorizontal: Boolean,
    ) {
        NONE(true),
        NEXT(true),
        PRE(true),
        UP(false),
        DOWN(false),
    }
}
