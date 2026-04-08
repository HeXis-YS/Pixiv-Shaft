package ceui.lisa.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import ceui.lisa.R

object ClipBoardUtils {

    @JvmStatic
    fun putTextIntoClipboard(context: Context, text: String) {
        putTextIntoClipboard(context, text, true)
    }

    @JvmStatic
    fun putTextIntoClipboard(context: Context, text: String, showHint: Boolean) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copy text", text)
        clipboardManager.setPrimaryClip(clipData)
        if (showHint) {
            Common.showToast(text + context.getString(R.string.has_copyed))
        }
    }

    @JvmStatic
    fun getClipboardContent(context: Context): String? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        if (cm != null) {
            val data = cm.primaryClip
            if (data != null && data.itemCount > 0) {
                val item = data.getItemAt(0)
                val sequence = item?.coerceToText(context)
                if (sequence != null) {
                    return sequence.toString()
                }
            }
        }
        return null
    }
}
