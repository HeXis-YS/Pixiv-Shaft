package ceui.lisa.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.models.Starable

abstract class BaseReceiver<Item : Starable>(
    protected var mAdapter: BaseAdapter<Item, *>?
) : BroadcastReceiver() {

    fun interface CallBack {
        fun onReceive(context: Context, intent: Intent)
    }
}
