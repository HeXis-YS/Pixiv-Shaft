package ceui.lisa.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import ceui.lisa.database.DownloadEntity
import ceui.lisa.interfaces.Callback
import ceui.lisa.model.Holder
import ceui.lisa.utils.Params

class DownloadReceiver<T>(
    private val mCallback: Callback<T>,
    private val type: Int,
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null) {
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                if (type == NOTIFY_FRAGMENT_DOWNLOADING) {
                    val holder = bundle.getSerializable(Params.CONTENT) as Holder
                    mCallback.doSomething(holder as T)
                } else if (type == NOTIFY_FRAGMENT_DOWNLOAD_FINISH) {
                    val downloadEntity = bundle.getSerializable(Params.CONTENT) as DownloadEntity
                    mCallback.doSomething(downloadEntity as T)
                }
            }
        }
    }

    companion object {
        const val NOTIFY_FRAGMENT_DOWNLOADING = 0
        const val NOTIFY_FRAGMENT_DOWNLOAD_FINISH = 1
    }
}
