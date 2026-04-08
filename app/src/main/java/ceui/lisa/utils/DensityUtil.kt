package ceui.lisa.utils

import android.content.res.Resources

class DensityUtil {

    @JvmField
    val density: Float = Resources.getSystem().displayMetrics.density

    fun dip2px(dpValue: Float): Int {
        return (0.5f + dpValue * density).toInt()
    }

    fun px2dip(pxValue: Int): Float {
        return pxValue / density
    }

    companion object {
        @JvmStatic
        fun dp2px(dpValue: Float): Int {
            return (0.5f + dpValue * Resources.getSystem().displayMetrics.density).toInt()
        }

        @JvmStatic
        fun px2dp(pxValue: Int): Float {
            return pxValue / Resources.getSystem().displayMetrics.density
        }
    }
}
