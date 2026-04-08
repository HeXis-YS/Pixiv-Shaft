package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class DynamicHeightImageView : AppCompatImageView {

    private var heightRatio = 0f
    private var tmpScaleType: ScaleType? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setHeightRatio(ratio: Float) {
        if (ratio != heightRatio) {
            heightRatio = ratio
            requestLayout()
        }
    }

    fun setHeightRatioAndScaleType(ratio: Float, scaleType: ScaleType?) {
        val ratioChanged = ratio != heightRatio
        if (ratioChanged) {
            heightRatio = ratio
            requestLayout()
        }
        tmpScaleType = scaleType
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (heightRatio > 0.0f) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = (width * heightRatio).toInt()
            setMeasuredDimension(width, height)
            if (tmpScaleType != null && tmpScaleType != scaleType) {
                setScaleType(tmpScaleType)
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
