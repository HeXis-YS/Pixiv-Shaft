package ceui.lisa.notification

import android.content.Context
import android.content.Intent
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.models.Starable
import ceui.lisa.utils.Params

class CommonReceiver(
    adapter: BaseAdapter<Starable, *>?
) : BaseReceiver<Starable>(adapter) {

    override fun onReceive(context: Context, intent: Intent) {
        if (mAdapter != null) {
            val bundle = intent.extras
            if (bundle != null) {
                val id = bundle.getInt(Params.ID)
                val isLiked = bundle.getBoolean(Params.IS_LIKED)
                mAdapter?.setLiked(id, isLiked)
            }
        }
    }
}
