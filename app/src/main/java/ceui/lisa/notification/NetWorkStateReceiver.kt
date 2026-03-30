package ceui.lisa.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ceui.lisa.core.Manager

class NetWorkStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("网络状态发生变化")
        Manager.get().stopAll()
    }
}
