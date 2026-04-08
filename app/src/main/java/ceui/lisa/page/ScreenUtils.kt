package ceui.lisa.page

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import ceui.lisa.activities.Shaft
import java.lang.reflect.Method

object ScreenUtils {

    @JvmStatic
    fun dpToPx(dp: Int): Int {
        val metrics = getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
    }

    @JvmStatic
    fun pxToDp(px: Int): Int {
        val metrics = getDisplayMetrics()
        return (px / metrics.density).toInt()
    }

    @JvmStatic
    fun spToPx(sp: Int): Int {
        val metrics = getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), metrics).toInt()
    }

    @JvmStatic
    fun pxToSp(px: Int): Int {
        val metrics = getDisplayMetrics()
        return (px / metrics.scaledDensity).toInt()
    }

    @JvmStatic
    fun getAppSize(): IntArray {
        val size = IntArray(2)
        val metrics = getDisplayMetrics()
        size[0] = metrics.widthPixels
        size[1] = metrics.heightPixels
        return size
    }

    @JvmStatic
    fun getScreenSize(activity: Activity): IntArray {
        val size = IntArray(2)
        val decorView: View = activity.window.decorView
        size[0] = decorView.width
        size[1] = decorView.height
        return size
    }

    @JvmStatic
    fun getStatusBarHeight(): Int {
        val resources: Resources = Shaft.getContext().resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    @JvmStatic
    fun getNavigationBarHeight(): Int {
        var navigationBarHeight = 0
        val resources = Shaft.getContext().resources
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (id > 0 && hasNavigationBar()) {
            navigationBarHeight = resources.getDimensionPixelSize(id)
        }
        return navigationBarHeight
    }

    private fun hasNavigationBar(): Boolean {
        var hasNavigationBar = false
        val resources = Shaft.getContext().resources
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        if (id > 0) {
            hasNavigationBar = resources.getBoolean(id)
        }
        try {
            @SuppressLint("PrivateApi")
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val method: Method = systemPropertiesClass.getMethod("get", String::class.java)
            val navBarOverride = method.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
            if ("1" == navBarOverride) {
                hasNavigationBar = false
            } else if ("0" == navBarOverride) {
                hasNavigationBar = true
            }
        } catch (_: Exception) {
        }
        return hasNavigationBar
    }

    @JvmStatic
    fun getDisplayMetrics(): DisplayMetrics {
        return Shaft.getContext().resources.displayMetrics
    }
}
