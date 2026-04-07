package ceui.lisa.helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity

object ShortcutHelper {
    @JvmStatic
    fun addAppShortcuts() {
        val context = Shaft.getContext()
        val searchShortcutId = "search"

        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        if (shortcuts.any { it.id == searchShortcutId }) {
            return
        }

        val intent = TemplateActivity.newSearchIntent(context).apply {
            action = Intent.ACTION_VIEW
        }
        val iconCompat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                IconCompat.createWithBitmap(getAdaptiveBitmap(context, R.mipmap.logo_final_round))
            } catch (_: Exception) {
                IconCompat.createWithResource(context, R.mipmap.logo_final_round)
            }
        } else {
            IconCompat.createWithResource(context, R.mipmap.logo_final_round)
        }
        val shortcut = ShortcutInfoCompat.Builder(context, searchShortcutId)
            .setShortLabel(context.getString(R.string.search))
            .setLongLabel(context.getString(R.string.search))
            .setIcon(iconCompat)
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getAdaptiveBitmap(context: Context, resId: Int): Bitmap {
        val drawable: Drawable = ResourcesCompat.getDrawable(context.resources, resId, null)
            ?: throw IllegalStateException("Missing shortcut drawable: $resId")

        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBitmap)

        val xferPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.RED
        }

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        maskCanvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), width / 2f, height / 2f, xferPaint)
        xferPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(maskBitmap, 0f, 0f, xferPaint)

        return bitmap
    }
}
