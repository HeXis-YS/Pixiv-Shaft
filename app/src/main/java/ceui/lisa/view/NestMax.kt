package ceui.lisa.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.core.widget.NestedScrollView

class NestMax : NestedScrollView {

    private val displayMetrics = DisplayMetrics()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val display = (context as Activity).windowManager.defaultDisplay
        display.getMetrics(displayMetrics)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxHeightMeasureSpec = heightMeasureSpec
        try {
            maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                displayMetrics.heightPixels / 2,
                MeasureSpec.AT_MOST
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec)
    }
}
