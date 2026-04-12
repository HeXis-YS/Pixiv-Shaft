package org.honorato.multistatetogglebutton

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView

class MultiStateToggleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private var listener: ToggleButton.OnValueChangedListener? = null
    private var elements: Array<String> = emptyArray()
    private var activeColor: Int = 0
    private var inactiveColor: Int = 0

    var value: Int = 0
        set(newValue) {
            field = newValue.coerceIn(0, (elements.size - 1).coerceAtLeast(0))
            syncSelection()
            listener?.onValueChanged(field)
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun setElements(elements: Array<String>) {
        this.elements = elements.copyOf()
        rebuild()
        syncSelection()
    }

    fun setColors(activeColor: Int, inactiveColor: Int) {
        this.activeColor = activeColor
        this.inactiveColor = inactiveColor
        syncSelection()
    }

    fun setOnValueChangedListener(listener: ToggleButton.OnValueChangedListener) {
        this.listener = listener
    }

    private fun rebuild() {
        removeAllViews()
        elements.forEachIndexed { index, label ->
            addView(
                AppCompatTextView(context).apply {
                    text = label
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                    setOnClickListener { value = index }
                    layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginEnd = if (index == elements.lastIndex) 0 else dp(8)
                    }
                },
            )
        }
    }

    private fun syncSelection() {
        repeat(childCount) { index ->
            val child = getChildAt(index) as? AppCompatTextView ?: return@repeat
            val selected = index == value
            child.isSelected = selected
            child.setTextColor(if (selected) activeColor else inactiveColor)
            child.setBackgroundColor(if (selected) activeColor else 0x00000000)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

class ToggleButton {
    interface OnValueChangedListener {
        fun onValueChanged(value: Int)
    }
}
