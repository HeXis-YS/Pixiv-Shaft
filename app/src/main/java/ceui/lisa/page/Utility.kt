package ceui.lisa.page

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import ceui.lisa.activities.Shaft

object Utility {
    private var seekRect: Rect? = null

    @JvmStatic
    fun px2sp(pxValue: Float): Int {
        val fontScale = Shaft.getContext().resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    @JvmStatic
    fun sp2px(spValue: Float): Int {
        val fontScale = Shaft.getContext().resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    @JvmStatic
    fun dip2px(dipValue: Float): Int {
        val scale = Shaft.getContext().resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun px2dip(pxValue: Float): Int {
        val scale = Shaft.getContext().resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    @JvmStatic
    fun addSeekBarTouchPoint(sb: SeekBar?) {
        if (sb == null) {
            return
        }

        try {
            val viewGroup = sb.parent as ViewGroup
            viewGroup.setOnTouchListener { _: View, event: MotionEvent ->
                seekRect = Rect()
                sb.getHitRect(seekRect)
                val rect = seekRect ?: return@setOnTouchListener false

                if (event.y >= rect.top - 50 && event.y <= rect.bottom + 50) {
                    val y = rect.top + rect.height() / 2f
                    var x = event.x - rect.left
                    if (x < 0) {
                        x = 0f
                    } else if (x > rect.width()) {
                        x = rect.width().toFloat()
                    }
                    val motionEvent = MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        event.action,
                        x,
                        y,
                        event.metaState,
                    )
                    return@setOnTouchListener sb.onTouchEvent(motionEvent)
                }
                false
            }
        } catch (_: Exception) {
        }
    }

    @JvmStatic
    fun openPreviewBook(context: Context, bookId: Int) {
    }
}
