package ceui.lisa.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.Drawable
import com.scwang.smart.drawable.PaintDrawable
import java.util.ArrayList

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class PathsDrawable : PaintDrawable() {
    protected var mWidth = 1
    protected var mHeight = 1
    protected var mStartX = 0
    protected var mStartY = 0
    protected var mOriginWidth = 0
    protected var mOriginHeight = 0
    protected var mPaths: List<Path>? = null
    protected var mColors: List<Int>? = null
    protected var mltOriginPath: List<Path>? = null
    protected var mltOriginSvg: List<String>? = null
    private var mCachedBitmap: Bitmap? = null
    private var mCacheDirty = false

    protected fun onMeasure(): Boolean {
        var top: Int? = null
        var left: Int? = null
        var right: Int? = null
        var bottom: Int? = null
        mPaths?.forEach { path ->
            REGION.setPath(path, MAX_CLIP)
            val bounds = REGION.bounds
            top = minOf(top ?: bounds.top, bounds.top)
            left = minOf(left ?: bounds.left, bounds.left)
            right = maxOf(right ?: bounds.right, bounds.right)
            bottom = maxOf(bottom ?: bounds.bottom, bounds.bottom)
        }

        mStartX = left ?: 0
        mStartY = top ?: 0
        mWidth = (right ?: 0) - mStartX
        mHeight = (bottom ?: 0) - mStartY

        if (mOriginWidth == 0) {
            mOriginWidth = mWidth
        }
        if (mOriginHeight == 0) {
            mOriginHeight = mHeight
        }

        val bounds = bounds
        return if (mWidth == 0 || mHeight == 0) {
            if (mOriginWidth == 0) {
                mOriginWidth = 1
            }
            if (mOriginHeight == 0) {
                mOriginHeight = 1
            }
            mWidth = 1
            mHeight = 1
            false
        } else {
            super.setBounds(bounds.left, bounds.top, bounds.left + mWidth, bounds.top + mHeight)
            true
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        val originPath = mltOriginPath
        if (originPath != null && originPath.isNotEmpty() && (width != mWidth || height != mHeight)) {
            val ox = mStartX
            val oy = mStartY
            val ratioWidth = 1f * width / mOriginWidth
            val ratioHeight = 1f * height / mOriginHeight
            mPaths = PathParser.transformScale(ratioWidth, ratioHeight, originPath, mltOriginSvg)
            if (!onMeasure()) {
                mWidth = width
                mHeight = height
                mStartX = (1f * ox * width / mOriginWidth).toInt()
                mStartY = (1f * oy * height / mOriginHeight).toInt()
                super.setBounds(left, top, right, bottom)
            }
        } else {
            super.setBounds(left, top, right, bottom)
        }
    }

    override fun setBounds(bounds: Rect) {
        setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    fun parserPaths(vararg paths: String): Boolean {
        mOriginWidth = 0
        mOriginHeight = 0
        val originSvg = ArrayList<String>()
        val parsedPaths = ArrayList<Path>()
        for (path in paths) {
            originSvg.add(path)
            parsedPaths.add(PathParser.createPathFromPathData(path)!!)
        }
        mltOriginSvg = originSvg
        mPaths = parsedPaths
        mltOriginPath = parsedPaths
        return onMeasure()
    }

    fun declareOriginal(startX: Int, startY: Int, width: Int, height: Int) {
        mStartX = startX
        mStartY = startY
        mOriginWidth = width
        mWidth = width
        mOriginHeight = height
        mHeight = height
        val bounds = bounds
        super.setBounds(bounds.left, bounds.top, bounds.left + width, bounds.top + height)
    }

    fun parserColors(vararg colors: Int) {
        val parsedColors = ArrayList<Int>()
        for (color in colors) {
            parsedColors.add(color)
        }
        mColors = parsedColors
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val width = bounds.width()
        val height = bounds.height()
        if (mPaint.alpha == 0xFF) {
            canvas.save()
            canvas.translate((bounds.left - mStartX).toFloat(), (bounds.top - mStartY).toFloat())
            mPaths?.forEachIndexed { index, path ->
                val colors = mColors
                if (colors != null && index < colors.size) {
                    mPaint.color = colors[index]
                }
                canvas.drawPath(path, mPaint)
            }
            mPaint.alpha = 0xFF
            canvas.restore()
        } else {
            createCachedBitmapIfNeeded(width, height)
            val cachedBitmap = mCachedBitmap ?: return
            if (mCacheDirty) {
                cachedBitmap.eraseColor(Color.TRANSPARENT)
                val tmpCanvas = Canvas(cachedBitmap)
                drawCachedBitmap(tmpCanvas)
                mCacheDirty = false
            }
            canvas.drawBitmap(cachedBitmap, bounds.left.toFloat(), bounds.top.toFloat(), mPaint)
        }
    }

    fun setGeometricWidth(width: Int) {
        val bounds = bounds
        val rate = 1f * width / bounds.width()
        setBounds(
            (bounds.left * rate).toInt(),
            (bounds.top * rate).toInt(),
            (bounds.right * rate).toInt(),
            (bounds.bottom * rate).toInt(),
        )
    }

    fun setGeometricHeight(height: Int) {
        val bounds = bounds
        val rate = 1f * height / bounds.height()
        setBounds(
            (bounds.left * rate).toInt(),
            (bounds.top * rate).toInt(),
            (bounds.right * rate).toInt(),
            (bounds.bottom * rate).toInt(),
        )
    }

    private fun drawCachedBitmap(canvas: Canvas) {
        canvas.translate((-mStartX).toFloat(), (-mStartY).toFloat())
        mPaths?.forEachIndexed { index, path ->
            val colors = mColors
            if (colors != null && index < colors.size) {
                mPaint.color = colors[index]
            }
            canvas.drawPath(path, mPaint)
        }
    }

    private fun createCachedBitmapIfNeeded(width: Int, height: Int) {
        val cachedBitmap = mCachedBitmap
        if (cachedBitmap == null || width != cachedBitmap.width || height != cachedBitmap.height) {
            mCachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mCacheDirty = true
        }
    }

    companion object {
        @JvmField
        protected val REGION: Region = Region()

        @JvmField
        protected val MAX_CLIP: Region = Region(Int.MIN_VALUE, Int.MIN_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }
}
