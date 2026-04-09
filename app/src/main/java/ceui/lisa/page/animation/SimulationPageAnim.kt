package ceui.lisa.page.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Region
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View

class SimulationPageAnim(
    w: Int,
    h: Int,
    view: View,
    listener: OnPageChangeListener,
) : HorizonPageAnim(w, h, view, listener) {
    private var mCornerX = 1
    private var mCornerY = 1
    private val mPath0 = Path()
    private val mPath1 = Path()

    private val mBezierStart1 = PointF()
    private val mBezierControl1 = PointF()
    private val mBeziervertex1 = PointF()
    private var mBezierEnd1 = PointF()

    private val mBezierStart2 = PointF()
    private val mBezierControl2 = PointF()
    private val mBeziervertex2 = PointF()
    private var mBezierEnd2 = PointF()

    private var mMiddleX = 0f
    private var mMiddleY = 0f
    private var mDegrees = 0f
    private var mTouchToCornerDis = 0f
    private val mColorMatrixFilter: ColorMatrixColorFilter
    private val mMatrix: Matrix
    private val mMatrixArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1.0f)

    private var mIsRTandLB = false
    private val mMaxLength = Math.hypot(mScreenWidth.toDouble(), mScreenHeight.toDouble()).toFloat()
    private lateinit var mBackShadowDrawableLR: GradientDrawable
    private lateinit var mBackShadowDrawableRL: GradientDrawable
    private lateinit var mFolderShadowDrawableLR: GradientDrawable
    private lateinit var mFolderShadowDrawableRL: GradientDrawable
    private lateinit var mFrontShadowDrawableHBT: GradientDrawable
    private lateinit var mFrontShadowDrawableHTB: GradientDrawable
    private lateinit var mFrontShadowDrawableVLR: GradientDrawable
    private lateinit var mFrontShadowDrawableVRL: GradientDrawable

    private val mPaint = Paint()
    private val mXORPath = Path()

    init {
        mPaint.style = Paint.Style.FILL
        createDrawable()

        val cm = ColorMatrix()
        cm.set(
            floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        mColorMatrixFilter = ColorMatrixColorFilter(cm)
        mMatrix = Matrix()

        mTouchX = 0.01f
        mTouchY = 0.01f
    }

    override fun drawMove(canvas: Canvas) {
        when (mDirection) {
            Direction.NEXT -> {
                calcPoints()
                drawCurrentPageArea(canvas, mCurBitmap, mPath0)
                drawNextPageAreaAndShadow(canvas, mNextBitmap)
                drawCurrentPageShadow(canvas)
                drawCurrentBackArea(canvas, mCurBitmap)
            }

            else -> {
                calcPoints()
                drawCurrentPageArea(canvas, mNextBitmap, mPath0)
                drawNextPageAreaAndShadow(canvas, mCurBitmap)
                drawCurrentPageShadow(canvas)
                drawCurrentBackArea(canvas, mNextBitmap)
            }
        }
    }

    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true)
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    override fun startAnim() {
        super.startAnim()
        var dx: Int
        val dy: Int
        if (isCancel) {
            dx =
                if (mCornerX > 0 && mDirection == Direction.NEXT) {
                    (mScreenWidth - mTouchX).toInt()
                } else {
                    -mTouchX.toInt()
                }

            if (mDirection != Direction.NEXT) {
                dx = -(mScreenWidth + mTouchX).toInt()
            }

            dy =
                if (mCornerY > 0) {
                    (mScreenHeight - mTouchY).toInt()
                } else {
                    -mTouchY.toInt()
                }
        } else {
            dx =
                if (mCornerX > 0 && mDirection == Direction.NEXT) {
                    -(mScreenWidth + mTouchX).toInt()
                } else {
                    (mScreenWidth - mTouchX + mScreenWidth).toInt()
                }
            dy =
                if (mCornerY > 0) {
                    (mScreenHeight - mTouchY).toInt()
                } else {
                    (1 - mTouchY).toInt()
                }
        }
        mScroller.startScroll(mTouchX.toInt(), mTouchY.toInt(), dx, dy, 400)
    }

    override fun setDirection(direction: Direction) {
        super.setDirection(direction)
        when (direction) {
            Direction.PRE -> {
                if (mStartX > mScreenWidth / 2) {
                    calcCornerXY(mStartX, mScreenHeight.toFloat())
                } else {
                    calcCornerXY(mScreenWidth - mStartX, mScreenHeight.toFloat())
                }
            }

            Direction.NEXT -> {
                if (mScreenWidth / 2 > mStartX) {
                    calcCornerXY(mScreenWidth - mStartX, mStartY)
                }
            }

            else -> Unit
        }
    }

    override fun setStartPoint(x: Float, y: Float) {
        super.setStartPoint(x, y)
        calcCornerXY(x, y)
    }

    override fun setTouchPoint(x: Float, y: Float) {
        super.setTouchPoint(x, y)
        if ((mStartY > mScreenHeight / 3 && mStartY < mScreenHeight * 2 / 3) || mDirection == Direction.PRE) {
            mTouchY = mScreenHeight.toFloat()
        }

        if (mStartY > mScreenHeight / 3 && mStartY < mScreenHeight / 2 && mDirection == Direction.NEXT) {
            mTouchY = 1f
        }
    }

    private fun createDrawable() {
        val color = intArrayOf(0x333333, 0xb0333333.toInt())
        mFolderShadowDrawableRL =
            GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }

        mFolderShadowDrawableLR =
            GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }

        val backShadowColors = intArrayOf(0xff111111.toInt(), 0x111111)
        mBackShadowDrawableRL =
            GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, backShadowColors).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }

        mBackShadowDrawableLR =
            GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, backShadowColors).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }

        val frontShadowColors = intArrayOf(0x80111111.toInt(), 0x111111)
        mFrontShadowDrawableVLR =
            GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, frontShadowColors).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }
        mFrontShadowDrawableVRL =
            GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, frontShadowColors).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }
        mFrontShadowDrawableHTB =
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, frontShadowColors).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }
        mFrontShadowDrawableHBT =
            GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, frontShadowColors).apply {
                gradientType = GradientDrawable.LINEAR_GRADIENT
            }
    }

    fun canDragOver(): Boolean {
        return mTouchToCornerDis > mScreenWidth / 10f
    }

    fun right(): Boolean {
        return mCornerX <= -4
    }

    private fun drawCurrentBackArea(canvas: Canvas, bitmap: Bitmap) {
        val i = (mBezierStart1.x + mBezierControl1.x).toInt() / 2
        val f1 = kotlin.math.abs(i - mBezierControl1.x)
        val i1 = (mBezierStart2.y + mBezierControl2.y).toInt() / 2
        val f2 = kotlin.math.abs(i1 - mBezierControl2.y)
        val f3 = kotlin.math.min(f1, f2)
        mPath1.reset()
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y)
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y)
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y)
        mPath1.lineTo(mTouchX, mTouchY)
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y)
        mPath1.close()

        val mFolderShadowDrawable: GradientDrawable
        val left: Int
        val right: Int
        if (mIsRTandLB) {
            left = (mBezierStart1.x - 1).toInt()
            right = (mBezierStart1.x + f3 + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableLR
        } else {
            left = (mBezierStart1.x - f3 - 1).toInt()
            right = (mBezierStart1.x + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableRL
        }
        canvas.save()
        try {
            canvas.clipPath(mPath0)
            canvas.clipPath(mPath1, Region.Op.INTERSECT)
        } catch (_: Exception) {
        }

        mPaint.colorFilter = mColorMatrixFilter
        val color = bitmap.getPixel(1, 1)
        val red = color and 0xff0000 shr 16
        val green = color and 0x00ff00 shr 8
        val blue = color and 0x0000ff
        val tempColor = Color.argb(200, red, green, blue)

        val dis = Math.hypot((mCornerX - mBezierControl1.x).toDouble(), (mBezierControl2.y - mCornerY).toDouble()).toFloat()
        val f8 = (mCornerX - mBezierControl1.x) / dis
        val f9 = (mBezierControl2.y - mCornerY) / dis
        mMatrixArray[0] = 1 - 2 * f9 * f9
        mMatrixArray[1] = 2 * f8 * f9
        mMatrixArray[3] = mMatrixArray[1]
        mMatrixArray[4] = 1 - 2 * f8 * f8
        mMatrix.reset()
        mMatrix.setValues(mMatrixArray)
        mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y)
        mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y)
        canvas.drawBitmap(bitmap, mMatrix, mPaint)
        canvas.drawColor(tempColor)

        mPaint.colorFilter = null

        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y)
        mFolderShadowDrawable.setBounds(
            left,
            mBezierStart1.y.toInt(),
            right,
            (mBezierStart1.y + mMaxLength).toInt(),
        )
        mFolderShadowDrawable.draw(canvas)
        canvas.restore()
    }

    private fun drawCurrentPageShadow(canvas: Canvas) {
        val degree =
            if (mIsRTandLB) {
                Math.PI / 4 - Math.atan2((mBezierControl1.y - mTouchY).toDouble(), (mTouchX - mBezierControl1.x).toDouble())
            } else {
                Math.PI / 4 - Math.atan2((mTouchY - mBezierControl1.y).toDouble(), (mTouchX - mBezierControl1.x).toDouble())
            }
        val d1 = 25f * 1.414f * kotlin.math.cos(degree).toFloat()
        val d2 = 25f * 1.414f * kotlin.math.sin(degree).toFloat()
        val x = mTouchX + d1
        val y = if (mIsRTandLB) mTouchY + d2 else mTouchY - d2

        mPath1.reset()
        mPath1.moveTo(x, y)
        mPath1.lineTo(mTouchX, mTouchY)
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y)
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y)
        mPath1.close()
        var rotateDegrees: Float
        canvas.save()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mXORPath.reset()
                mXORPath.moveTo(0f, 0f)
                mXORPath.lineTo(canvas.width.toFloat(), 0f)
                mXORPath.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
                mXORPath.lineTo(0f, canvas.height.toFloat())
                mXORPath.close()
                mXORPath.op(mPath0, Path.Op.XOR)
                canvas.clipPath(mXORPath)
            } else {
                canvas.clipPath(mPath0, Region.Op.XOR)
            }
            canvas.clipPath(mPath1, Region.Op.INTERSECT)
        } catch (_: Exception) {
        }

        var leftx: Int
        var rightx: Int
        var currentPageShadow: GradientDrawable
        if (mIsRTandLB) {
            leftx = mBezierControl1.x.toInt()
            rightx = mBezierControl1.x.toInt() + 25
            currentPageShadow = mFrontShadowDrawableVLR
        } else {
            leftx = (mBezierControl1.x - 25).toInt()
            rightx = mBezierControl1.x.toInt() + 1
            currentPageShadow = mFrontShadowDrawableVRL
        }

        rotateDegrees =
            Math.toDegrees(
                Math.atan2(
                    (mTouchX - mBezierControl1.x).toDouble(),
                    (mBezierControl1.y - mTouchY).toDouble(),
                ),
            ).toFloat()
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y)
        currentPageShadow.setBounds(
            leftx,
            (mBezierControl1.y - mMaxLength).toInt(),
            rightx,
            mBezierControl1.y.toInt(),
        )
        currentPageShadow.draw(canvas)
        canvas.restore()

        mPath1.reset()
        mPath1.moveTo(x, y)
        mPath1.lineTo(mTouchX, mTouchY)
        mPath1.lineTo(mBezierControl2.x, mBezierControl2.y)
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y)
        mPath1.close()
        canvas.save()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mXORPath.reset()
                mXORPath.moveTo(0f, 0f)
                mXORPath.lineTo(canvas.width.toFloat(), 0f)
                mXORPath.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
                mXORPath.lineTo(0f, canvas.height.toFloat())
                mXORPath.close()
                mXORPath.op(mPath0, Path.Op.XOR)
                canvas.clipPath(mXORPath)
            } else {
                canvas.clipPath(mPath0, Region.Op.XOR)
            }
            canvas.clipPath(mPath1, Region.Op.INTERSECT)
        } catch (_: Exception) {
        }

        if (mIsRTandLB) {
            leftx = mBezierControl2.y.toInt()
            rightx = mBezierControl2.y.toInt() + 25
            currentPageShadow = mFrontShadowDrawableHTB
        } else {
            leftx = (mBezierControl2.y - 25).toInt()
            rightx = mBezierControl2.y.toInt() + 1
            currentPageShadow = mFrontShadowDrawableHBT
        }
        rotateDegrees =
            Math.toDegrees(
                Math.atan2(
                    (mBezierControl2.y - mTouchY).toDouble(),
                    (mBezierControl2.x - mTouchX).toDouble(),
                ),
            ).toFloat()
        canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y)
        val temp =
            if (mBezierControl2.y < 0) {
                mBezierControl2.y - mScreenHeight
            } else {
                mBezierControl2.y
            }

        val hmg = Math.hypot(mBezierControl2.x.toDouble(), temp.toDouble()).toInt()
        if (hmg > mMaxLength) {
            currentPageShadow.setBounds(
                (mBezierControl2.x - 25).toInt() - hmg,
                leftx,
                (mBezierControl2.x + mMaxLength).toInt() - hmg,
                rightx,
            )
        } else {
            currentPageShadow.setBounds(
                (mBezierControl2.x - mMaxLength).toInt(),
                leftx,
                mBezierControl2.x.toInt(),
                rightx,
            )
        }

        currentPageShadow.draw(canvas)
        canvas.restore()
    }

    private fun drawNextPageAreaAndShadow(canvas: Canvas, bitmap: Bitmap) {
        mPath1.reset()
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y)
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y)
        mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y)
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y)
        mPath1.lineTo(mCornerX.toFloat(), mCornerY.toFloat())
        mPath1.close()

        mDegrees =
            Math.toDegrees(
                Math.atan2(
                    (mBezierControl1.x - mCornerX).toDouble(),
                    (mBezierControl2.y - mCornerY).toDouble(),
                ),
            ).toFloat()
        val leftx: Int
        val rightx: Int
        val backShadowDrawable: GradientDrawable
        if (mIsRTandLB) {
            leftx = mBezierStart1.x.toInt()
            rightx = (mBezierStart1.x + mTouchToCornerDis / 4).toInt()
            backShadowDrawable = mBackShadowDrawableLR
        } else {
            leftx = (mBezierStart1.x - mTouchToCornerDis / 4).toInt()
            rightx = mBezierStart1.x.toInt()
            backShadowDrawable = mBackShadowDrawableRL
        }
        canvas.save()
        try {
            canvas.clipPath(mPath0)
            canvas.clipPath(mPath1, Region.Op.INTERSECT)
        } catch (_: Exception) {
        }

        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y)
        backShadowDrawable.setBounds(
            leftx,
            mBezierStart1.y.toInt(),
            rightx,
            (mMaxLength + mBezierStart1.y).toInt(),
        )
        backShadowDrawable.draw(canvas)
        canvas.restore()
    }

    private fun drawCurrentPageArea(canvas: Canvas, bitmap: Bitmap, path: Path) {
        mPath0.reset()
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y)
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y)
        mPath0.lineTo(mTouchX, mTouchY)
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y)
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y)
        mPath0.lineTo(mCornerX.toFloat(), mCornerY.toFloat())
        mPath0.close()

        canvas.save()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mXORPath.reset()
            mXORPath.moveTo(0f, 0f)
            mXORPath.lineTo(canvas.width.toFloat(), 0f)
            mXORPath.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
            mXORPath.lineTo(0f, canvas.height.toFloat())
            mXORPath.close()
            mXORPath.op(path, Path.Op.XOR)
            canvas.clipPath(mXORPath)
        } else {
            canvas.clipPath(path, Region.Op.XOR)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        try {
            canvas.restore()
        } catch (_: Exception) {
        }
    }

    fun calcCornerXY(x: Float, y: Float) {
        mCornerX = if (x <= mScreenWidth / 2f) 0 else mScreenWidth
        mCornerY = if (y <= mScreenHeight / 2f) 0 else mScreenHeight
        mIsRTandLB =
            (mCornerX == 0 && mCornerY == mScreenHeight) ||
                (mCornerX == mScreenWidth && mCornerY == 0)
    }

    private fun calcPoints() {
        mMiddleX = (mTouchX + mCornerX) / 2
        mMiddleY = (mTouchY + mCornerY) / 2
        mBezierControl1.x =
            mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX)
        mBezierControl1.y = mCornerY.toFloat()
        mBezierControl2.x = mCornerX.toFloat()

        val f4 = mCornerY - mMiddleY
        mBezierControl2.y =
            if (f4 == 0f) {
                mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / 0.1f
            } else {
                mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY)
            }
        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2
        mBezierStart1.y = mCornerY.toFloat()

        if (mTouchX > 0 && mTouchX < mScreenWidth) {
            if (mBezierStart1.x < 0 || mBezierStart1.x > mScreenWidth) {
                if (mBezierStart1.x < 0) {
                    mBezierStart1.x = mScreenWidth - mBezierStart1.x
                }

                val f1 = kotlin.math.abs(mCornerX - mTouchX)
                val f2 = mScreenWidth * f1 / mBezierStart1.x
                mTouchX = kotlin.math.abs(mCornerX - f2)

                val f3 = kotlin.math.abs(mCornerX - mTouchX) * kotlin.math.abs(mCornerY - mTouchY) / f1
                mTouchY = kotlin.math.abs(mCornerY - f3)

                mMiddleX = (mTouchX + mCornerX) / 2
                mMiddleY = (mTouchY + mCornerY) / 2

                mBezierControl1.x =
                    mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX)
                mBezierControl1.y = mCornerY.toFloat()
                mBezierControl2.x = mCornerX.toFloat()

                val f5 = mCornerY - mMiddleY
                mBezierControl2.y =
                    if (f5 == 0f) {
                        mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / 0.1f
                    } else {
                        mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY)
                    }

                mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2
            }
        }
        mBezierStart2.x = mCornerX.toFloat()
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2

        mTouchToCornerDis = Math.hypot((mTouchX - mCornerX).toDouble(), (mTouchY - mCornerY).toDouble()).toFloat()

        mBezierEnd1 =
            getCross(PointF(mTouchX, mTouchY), mBezierControl1, mBezierStart1, mBezierStart2)
        mBezierEnd2 =
            getCross(PointF(mTouchX, mTouchY), mBezierControl2, mBezierStart1, mBezierStart2)

        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4
    }

    private fun getCross(p1: PointF, p2: PointF, p3: PointF, p4: PointF): PointF {
        val crossP = PointF()
        val a1 = (p2.y - p1.y) / (p2.x - p1.x)
        val b1 = (p1.x * p2.y - p2.x * p1.y) / (p1.x - p2.x)

        val a2 = (p4.y - p3.y) / (p4.x - p3.x)
        val b2 = (p3.x * p4.y - p4.x * p3.y) / (p3.x - p4.x)
        crossP.x = (b2 - b1) / (a1 - a2)
        crossP.y = a1 * crossP.x + b1
        return crossP
    }
}
