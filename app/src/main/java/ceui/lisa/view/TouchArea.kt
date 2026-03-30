package ceui.lisa.view

class TouchArea(
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
) {
    fun set(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }
}
