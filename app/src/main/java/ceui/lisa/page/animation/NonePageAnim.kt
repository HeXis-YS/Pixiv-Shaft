package ceui.lisa.page.animation

import android.graphics.Canvas
import android.view.View

class NonePageAnim(
    w: Int,
    h: Int,
    view: View,
    listener: OnPageChangeListener
) : HorizonPageAnim(w, h, view, listener) {
    var downX = 0f
    var downY = 0f
    var isMove = false
    var slop = 0
    var hasPreOrNext = false

    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    override fun drawMove(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    override fun startAnim() {}
}
