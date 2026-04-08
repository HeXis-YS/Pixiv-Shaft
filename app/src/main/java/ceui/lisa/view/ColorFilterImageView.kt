package ceui.lisa.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.makeramen.roundedimageview.RoundedImageView

class ColorFilterImageView : RoundedImageView, View.OnTouchListener {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> setColorFilter(Color.TRANSPARENT)
        }
        return false
    }
}
