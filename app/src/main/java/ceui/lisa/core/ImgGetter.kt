package ceui.lisa.core

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.TextView
import ceui.lisa.utils.Common
import org.sufficientlysecure.htmltextview.HtmlAssetsImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.io.IOException

class ImgGetter : HtmlAssetsImageGetter {

    private val context: Context

    constructor(context: Context) : super(context) {
        this.context = context
    }

    constructor(textView: TextView) : super(textView) {
        this.context = textView.context
    }

    override fun getDrawable(source: String): Drawable? {
        return try {
            context.assets.open(source).use { inputStream ->
                val drawable = Drawable.createFromStream(inputStream, null) ?: return null
                drawable.setBounds(0, 0, BOUND, BOUND)
                Common.showLog("wid: ${drawable.intrinsicWidth} heightL: ${drawable.intrinsicHeight}")
                drawable
            }
        } catch (e: IOException) {
            Log.e(HtmlTextView.TAG, "source could not be found: $source")
            null
        }
    }

    companion object {
        const val BOUND = 54
    }
}
