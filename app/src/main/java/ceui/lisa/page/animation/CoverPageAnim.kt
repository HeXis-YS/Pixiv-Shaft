package ceui.lisa.page.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View

class CoverPageAnim(
    w: Int,
    h: Int,
    view: View,
    listener: OnPageChangeListener
) : HorizonPageAnim(w, h, view, listener) {
    private val mSrcRect = Rect(0, 0, mViewWidth, mViewHeight)
    private val mDestRect = Rect(0, 0, mViewWidth, mViewHeight)
    private val mBackShadowDrawableLR: GradientDrawable

    init {
        val mBackShadowColors = intArrayOf(0x66000000, 0x00000000)
        mBackShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            mBackShadowColors
        )
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    }

    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true)
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    override fun drawMove(canvas: Canvas) {
        when (mDirection) {
            Direction.NEXT -> {
                var dis = (mViewWidth - mStartX + mTouchX).toInt()
                if (dis > mViewWidth) {
                    dis = mViewWidth
                }
                mSrcRect.left = mViewWidth - dis
                mDestRect.right = dis
                canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null)
                addShadow(dis, canvas)
            }

            else -> {
                mSrcRect.left = (mViewWidth - mTouchX).toInt()
                mDestRect.right = mTouchX.toInt()
                canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
                canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null)
                addShadow(mTouchX.toInt(), canvas)
            }
        }
    }

    fun addShadow(left: Int, canvas: Canvas) {
        mBackShadowDrawableLR.setBounds(left, 0, left + 30, mScreenHeight)
        mBackShadowDrawableLR.draw(canvas)
    }

    override fun startAnim() {
        super.startAnim()
        val dx = when (mDirection) {
            Direction.NEXT -> {
                if (isCancel) {
                    var dis = ((mViewWidth - mStartX) + mTouchX).toInt()
                    if (dis > mViewWidth) {
                        dis = mViewWidth
                    }
                    mViewWidth - dis
                } else {
                    -(mTouchX + (mViewWidth - mStartX)).toInt()
                }
            }

            else -> {
                if (isCancel) {
                    -mTouchX.toInt()
                } else {
                    (mViewWidth - mTouchX).toInt()
                }
            }
        }

        val duration = (400 * kotlin.math.abs(dx)) / mViewWidth
        mScroller.startScroll(mTouchX.toInt(), 0, dx, 0, duration)
    }
}
