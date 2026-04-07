package ceui.lisa.helper

import androidx.annotation.NonNull
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import com.blankj.utilcode.util.ScreenUtils
import java.lang.reflect.Field
import kotlin.math.max

object DrawerLayoutHelper {
    @JvmStatic
    fun setCustomLeftEdgeSize(@NonNull drawerLayout: DrawerLayout, displayWidthPercentage: Float) {
        try {
            val leftDraggerField = drawerLayout.javaClass.getDeclaredField("mLeftDragger")
            leftDraggerField.isAccessible = true
            val leftDragger = leftDraggerField[drawerLayout] as ViewDragHelper

            val edgeSizeField = leftDragger.javaClass.getDeclaredField("mEdgeSize")
            edgeSizeField.isAccessible = true
            val edgeSize = edgeSizeField.getInt(leftDragger)
            val widthPixels = ScreenUtils.getScreenWidth()
            edgeSizeField.setInt(leftDragger, max(edgeSize, (widthPixels * displayWidthPercentage).toInt()))

            val leftCallbackField = drawerLayout.javaClass.getDeclaredField("mLeftCallback")
            leftCallbackField.isAccessible = true
            val leftCallback = leftCallbackField[drawerLayout] as ViewDragHelper.Callback

            val peekRunnableField = leftCallback.javaClass.getDeclaredField("mPeekRunnable")
            peekRunnableField.isAccessible = true
            peekRunnableField.set(leftCallback, Runnable {})
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}
