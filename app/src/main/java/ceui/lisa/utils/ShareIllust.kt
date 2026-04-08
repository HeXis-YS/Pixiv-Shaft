package ceui.lisa.utils

import android.content.Context
import android.content.Intent
import ceui.lisa.R
import ceui.lisa.interfaces.IExecutor
import ceui.lisa.models.IllustsBean

abstract class ShareIllust(
    private val context: Context,
    private val illustsBean: IllustsBean
) : IExecutor {

    override fun execute() {
        onPrepare()
        share()
    }

    private fun share() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            context.getString(
                R.string.share_illust,
                illustsBean.title,
                illustsBean.user.name,
                URL_Head + illustsBean.id
            )
        )
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
    }

    companion object {
        @JvmField
        val URL_Head = "https://www.pixiv.net/artworks/"
    }
}
