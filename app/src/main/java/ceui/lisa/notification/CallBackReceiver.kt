package ceui.lisa.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallBackReceiver(
    private val mCallBack: BaseReceiver.CallBack?
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (mCallBack != null) {
            mCallBack.onReceive(context, intent)
        }
    }
}
