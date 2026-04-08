package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT

class ExpandCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(context, attrs, defStyleAttr) {

    private var expandState = true
    private var maxHeight = 0

    init {
        init(context)
    }

    private fun init(context: Context) {
        maxHeight = context.resources.displayMetrics.heightPixels * 7 / 10
    }

    fun open() {
        if (expandState) {
            return
        }

        val layoutParams: ViewGroup.LayoutParams = layoutParams
        layoutParams.height = WRAP_CONTENT
        for (i in 0 until childCount) {
            if (getChildAt(i) is RecyclerView) {
                val recyclerView = getChildAt(i) as RecyclerView
                if (recyclerView.layoutManager is ScrollChange) {
                    (recyclerView.layoutManager as ScrollChange).setScrollEnabled(true)
                    break
                }
            }
        }
        setLayoutParams(layoutParams)
        expandState = true
    }

    fun close() {
        if (expandState) {
            val layoutParams: ViewGroup.LayoutParams = layoutParams
            layoutParams.height = maxHeight
            for (i in 0 until childCount) {
                if (getChildAt(i) is RecyclerView) {
                    val recyclerView = getChildAt(i) as RecyclerView
                    if (recyclerView.layoutManager is ScrollChange) {
                        (recyclerView.layoutManager as ScrollChange).setScrollEnabled(false)
                    }
                }
            }
            setLayoutParams(layoutParams)
            expandState = false
        }
    }

    fun isExpand(): Boolean {
        return expandState
    }
}
