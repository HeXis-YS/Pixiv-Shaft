package ceui.lisa.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ScrollView
import android.widget.TextView

class ContextMenuTitleView : ScrollView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, title: String) : super(context) {
        initTitleView(context, title, Color.BLACK)
    }

    constructor(context: Context, title: String, colorId: Int) : super(context) {
        initTitleView(context, title, colorId)
    }

    private fun initTitleView(context: Context, title: String, color: Int) {
        val padding = dpToPx(PADDING_DP)
        setPadding(padding, padding, padding, 0)

        val titleView = TextView(context)
        titleView.text = title
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        titleView.setTextColor(color)
        addView(titleView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxHeightSpec = MeasureSpec.makeMeasureSpec(dpToPx(MAX_HEIGHT_DP), MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, maxHeightSpec)
    }

    companion object {
        private const val MAX_HEIGHT_DP = 70
        private const val PADDING_DP = 16

        @JvmStatic
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
