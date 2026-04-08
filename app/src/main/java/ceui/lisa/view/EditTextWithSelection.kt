package ceui.lisa.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class EditTextWithSelection : AppCompatEditText {

    private var onSelectionChangeListener: OnSelectionChange? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        onSelectionChangeListener?.onChange(selStart, selEnd)
        super.onSelectionChanged(selStart, selEnd)
    }

    fun getOnSelectionChange(): OnSelectionChange? {
        return onSelectionChangeListener
    }

    fun setOnSelectionChange(onSelectionChange: OnSelectionChange?) {
        onSelectionChangeListener = onSelectionChange
    }

    interface OnSelectionChange {
        fun onChange(start: Int, end: Int)
    }
}
