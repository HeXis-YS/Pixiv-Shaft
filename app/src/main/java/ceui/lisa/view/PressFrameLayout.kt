package ceui.lisa.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import ceui.lisa.utils.DensityUtil

class PressFrameLayout : RelativeLayout {
    private var widthValue = 0
    private var heightValue = 0
    private val padding = DensityUtil().dip2px(20f)
    private val cornerRadius = DensityUtil().dip2px(5f)
    private val shadeOffset = DensityUtil().dip2px(5f).toFloat()
    var paintBg = Paint(Paint.ANTI_ALIAS_FLAG)
    var camera = Camera()
    var cameraX = 0f
    var cameraY = 0f
    private var colorBg = 0
    private val shadeAlpha = 0xaa000000.toInt()
    private val pressArea = TouchArea(0f, 0f, 0f, 0f)
    var isInPressArea = true
    private val maxAngle = 5
    private val scale = 0.98f
    private var pressTime = 0L
    var bitmap: Bitmap? = null
    var srcRectF = Rect()
    var dstRectF = RectF()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)

        val background: Drawable? = background
        if (background is ColorDrawable) {
            colorBg = background.color
            paintBg.color = colorBg
        } else if (background is BitmapDrawable) {
            bitmap = background.bitmap
            bitmap?.let {
                srcRectF = Rect(0, 0, it.width, it.height)
            }
        }
        setBackground(null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInPressArea) {
            camera.save()
            camera.rotateX(maxAngle * cameraX * cameraProgress)
            camera.rotateY(maxAngle * cameraY * cameraProgress)
            canvas.translate(widthValue / 2f, heightValue / 2f)
            camera.applyToCanvas(canvas)
            canvas.translate(-widthValue / 2f, -heightValue / 2f)
            camera.restore()
        }
        paintBg.setShadowLayer(shadeOffset * touchProgress, 0f, 0f, (colorBg and 0x00FFFFFF) or shadeAlpha)
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            canvas.drawBitmap(currentBitmap, srcRectF, dstRectF, paintBg)
        } else {
            canvas.drawRoundRect(dstRectF, cornerRadius.toFloat(), cornerRadius.toFloat(), paintBg)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        heightValue = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        widthValue = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        dstRectF.set(
            padding.toFloat(),
            padding.toFloat(),
            (widthValue - padding).toFloat(),
            (heightValue - padding).toFloat(),
        )
        pressArea.set(
            (widthValue - 2 * padding) / 4f + padding,
            (heightValue - 2 * padding) / 4f + padding,
            widthValue - (widthValue - 2 * padding) / 4f - padding,
            heightValue - (widthValue - 2 * padding) / 4f - padding,
        )
    }

    private fun isInsidePressArea(x: Float, y: Float): Boolean {
        return x > pressArea.left && x < pressArea.right &&
            y > pressArea.top && y < pressArea.bottom
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val animatorSet = AnimatorSet()
        val duration = 100L
        var type = 0
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                type = 1
                isInPressArea = isInsidePressArea(event.x, event.y)
            }

            MotionEvent.ACTION_CANCEL -> {
                type = 2
            }

            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - pressTime < 500) {
                    performClick()
                }
                type = 2
            }
        }
        if (isInPressArea) {
            if (type != 0) {
                val animX = ObjectAnimator.ofFloat(this, "scaleX", if (type == 1) 1f else scale, if (type == 1) scale else 1f)
                    .setDuration(duration)
                val animY = ObjectAnimator.ofFloat(this, "scaleY", if (type == 1) 1f else scale, if (type == 1) scale else 1f)
                    .setDuration(duration)
                val animZ = ObjectAnimator.ofFloat(this, "touchProgress", if (type == 1) 1f else 0f, if (type == 1) 0f else 1f)
                    .setDuration(duration)
                val interpolator = DecelerateInterpolator()
                animX.interpolator = interpolator
                animY.interpolator = interpolator
                animZ.interpolator = interpolator
                animatorSet.playTogether(animX, animY, animZ)
                animatorSet.start()
            }
        } else {
            cameraX = (event.x - widthValue / 2f) / ((widthValue - 2 * padding) / 2f)
            if (cameraX > 1) cameraX = 1f
            if (cameraX < -1) cameraX = -1f

            cameraY = (event.y - heightValue / 2f) / ((heightValue - 2 * padding) / 2f)
            if (cameraY > 1) cameraY = 1f
            if (cameraY < -1) cameraY = -1f

            val tmp = cameraX
            cameraX = -cameraY
            cameraY = tmp
            when (type) {
                1 -> ObjectAnimator.ofFloat(this, "cameraProgress", 0f, 1f).setDuration(duration).start()
                2 -> ObjectAnimator.ofFloat(this, "cameraProgress", 1f, 0f).setDuration(duration).start()
            }
            invalidate()
        }
        return true
    }

    var touchProgress = 1f
        set(value) {
            field = value
            invalidate()
        }

    var cameraProgress = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun performClick(): Boolean = super.performClick()
}
